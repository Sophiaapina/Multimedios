package controller;

import model.MediaItem;
import org.json.JSONObject;
import service.ExifService;
import service.GeocodingService;
import service.MapService;
import service.OpenAIService;
import service.VideoAssemblerService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VideoCreatorController {

    private final String apiKey;
    private final BiConsumer<Integer, Integer> onProgress;
    private final Consumer<String> onLog;

    private final ExifService exifService;
    private final OpenAIService openAIService;
    private final GeocodingService geocodingService;
    private final MapService mapService;

    public VideoCreatorController(String apiKey,
                                  BiConsumer<Integer, Integer> onProgress,
                                  Consumer<String> onLog) {
        this.apiKey = apiKey;
        this.onProgress = onProgress;
        this.onLog = onLog;

        this.exifService = new ExifService();
        this.openAIService = new OpenAIService(apiKey);
        this.geocodingService = new GeocodingService();
        this.mapService = new MapService();
    }

    public void createVideo(List<File> inputFiles, File outputVideo) throws Exception {
        int totalSteps = 7;
        int step = 0;

        progress(++step, totalSteps, "Reading media metadata...");
        List<MediaItem> items = parseAndSort(inputFiles);
        validateGps(items);

        File workDir = createWorkDir();

        progress(++step, totalSteps, "Generating cover image...");
        File coverImage = new File(workDir, "cover.png");
        List<String> descriptions = buildDescriptions(items);
        openAIService.generateCoverImage(descriptions, coverImage);

        progress(++step, totalSteps, "Identifying locations...");
        MediaItem firstGps = findFirstWithGps(items);
        MediaItem lastGps = findLastWithGps(items);

        String firstPlace = firstGps != null
                ? geocodingService.reverseGeocode(firstGps.getLatitude(), firstGps.getLongitude())
                : "the start";

        String lastPlace = lastGps != null
                ? geocodingService.reverseGeocode(lastGps.getLatitude(), lastGps.getLongitude())
                : "the destination";

        log("First location: " + firstPlace + " | Last location: " + lastPlace);

        progress(++step, totalSteps, "Generating AI narration and phrase...");
        GeneratedText generatedText = generateNarrationAndPhrase(items, firstPlace, lastPlace);
        String narration = generatedText.narration();
        String phrase = generatedText.phrase();

        progress(++step, totalSteps, "Generating map image...");
        File mapImage = new File(workDir, "map.png");


        if (firstGps != null && lastGps != null) {
            mapService.generateMapImage(
                    firstGps.getLatitude(),
                    firstGps.getLongitude(),
                    lastGps.getLatitude(),
                    lastGps.getLongitude(),
                    phrase,
                    mapImage,
                    firstPlace,
                    lastPlace
            );
        } else {
            log("No GPS data available for map.");
        }

        progress(++step, totalSteps, "Generating AI narration audio...");
        File narrationAudio = new File(workDir, "full_narration.wav");
        openAIService.textToSpeech(narration, narrationAudio);

        progress(++step, totalSteps, "Assembling final video...");
        VideoAssemblerService assembler = new VideoAssemblerService(workDir, this::log);
        assembler.assemble(
                coverImage,
                items,
                mapImage,
                narrationAudio,
                outputVideo
        );

        progress(totalSteps, totalSteps, "Done! Video saved to: " + outputVideo.getAbsolutePath());
    }

    private GeneratedText generateNarrationAndPhrase(List<MediaItem> items,
                                                     String firstPlace,
                                                     String lastPlace) {
        try {
            JSONObject all = openAIService.generateAllContent(items, firstPlace, lastPlace);

            String narration = all.optString("narration", "").trim();
            String phrase = all.optString("phrase", "").trim();

            if (narration.isBlank()) {
                narration = buildFallbackNarration(firstPlace, lastPlace);
            }
            if (phrase.isBlank()) {
                phrase = buildFallbackPhrase();
            }

            return new GeneratedText(narration, phrase);
        } catch (Exception e) {
            log("OpenAI failed, using fallback text. " + e.getMessage());
            return new GeneratedText(
                    buildFallbackNarration(firstPlace, lastPlace),
                    buildFallbackPhrase()
            );
        }
    }

    private String buildFallbackNarration(String firstPlace, String lastPlace) {
        return "Welcome to this journey. " +
                "This video presents a collection of moments captured along the way. " +
                "The trip began at " + firstPlace + " and ended at " + lastPlace + ". " +
                "Each image and video preserves a different part of the experience. " +
                "Together, these places form a memorable story.";
    }

    private String buildFallbackPhrase() {
        return "Every journey leaves a mark between where we begin and where we end.";
    }

    private List<MediaItem> parseAndSort(List<File> files) {
        List<MediaItem> items = new ArrayList<>();

        for (File f : files) {
            MediaItem.Type type = isVideo(f) ? MediaItem.Type.VIDEO : MediaItem.Type.IMAGE;
            MediaItem item = new MediaItem(f, type);
            exifService.populate(item);
            items.add(item);
            log("Loaded: " + item);
        }

        items.sort(Comparator.comparing(
                item -> item.getDateTime() != null ? item.getDateTime() : java.time.LocalDateTime.MIN
        ));

        return items;
    }

    private void validateGps(List<MediaItem> items) {
        long gpsCount = items.stream().filter(MediaItem::hasGps).count();

        if (gpsCount == 0) {
            log("WARNING: No media items have GPS data. Map will use default coordinates.");
        } else if (gpsCount == 1) {
            log("WARNING: Only one item has GPS data. Start and end pins may overlap.");
        }
    }

    private List<String> buildDescriptions(List<MediaItem> items) {
        List<String> desc = new ArrayList<>();
        for (MediaItem item : items) {
            String name = item.getFile().getName();
            String gps = item.hasGps()
                    ? String.format("(%.2f, %.2f)", item.getLatitude(), item.getLongitude())
                    : "";
            desc.add(item.getType() + " " + name + " " + gps);
        }
        return desc;
    }

    private MediaItem findFirstWithGps(List<MediaItem> items) {
        return items.stream().filter(MediaItem::hasGps).findFirst().orElse(null);
    }

    private MediaItem findLastWithGps(List<MediaItem> items) {
        MediaItem last = null;
        for (MediaItem item : items) {
            if (item.hasGps()) {
                last = item;
            }
        }
        return last;
    }

    private File createWorkDir() throws IOException {
        File dir = Files.createTempDirectory("gps_video_").toFile();
        dir.deleteOnExit();
        return dir;
    }

    private boolean isVideo(File file) {
        String lower = file.getName().toLowerCase();
        return lower.endsWith(".mp4")
                || lower.endsWith(".mov")
                || lower.endsWith(".avi")
                || lower.endsWith(".mkv")
                || lower.endsWith(".m4v")
                || lower.endsWith(".3gp");
    }

    private void progress(int current, int total, String message) {
        log(message);
        onProgress.accept(current, total);
    }

    private void log(String message) {
        onLog.accept(message);
    }

    private record GeneratedText(String narration, String phrase) {}
}