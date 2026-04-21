package service;

import model.MediaItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VideoAssemblerService {

    private static final int VIDEO_W = 1080;
    private static final int VIDEO_H = 1920;
    private static final int IMAGE_DURATION_SEC = 5;

    private final File workDir;
    private final Consumer<String> logger;

    public VideoAssemblerService(File workDir, Consumer<String> logger) {
        this.workDir = workDir;
        this.logger = logger;
    }

    public void assemble(File coverImage,
                         List<MediaItem> mediaItems,
                         File mapImage,
                         File narrationAudio,
                         File outputVideo) throws IOException, InterruptedException {

        logger.accept("Preparing silent video segments...");

        List<File> segments = new ArrayList<>();

        segments.add(buildImageSegmentSilent(coverImage, "cover", IMAGE_DURATION_SEC));

        for (int i = 0; i < mediaItems.size(); i++) {
            MediaItem item = mediaItems.get(i);
            String tag = "item_" + i;

            File segment;
            switch (item.getType()) {
                case IMAGE:
                    segment = buildImageSegmentSilent(item.getFile(), tag, IMAGE_DURATION_SEC);
                    break;
                case VIDEO:
                    segment = buildVideoSegmentSilent(item, tag);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported media type: " + item.getType());
            }

            segments.add(segment);
            logger.accept("Segment " + (i + 1) + "/" + mediaItems.size() + " prepared.");
        }

        segments.add(buildImageSegmentSilent(mapImage, "map", IMAGE_DURATION_SEC));

        logger.accept("Concatenating silent segments...");
        File rawVideo = new File(workDir, "raw_visual_video.mp4");
        concatenateSegments(segments, rawVideo);

        logger.accept("Adding final narration and normalizing audio...");
        addNarrationAndNormalize(rawVideo, narrationAudio, outputVideo);

        logger.accept("Video assembly complete: " + outputVideo.getAbsolutePath());
    }

    private File buildImageSegmentSilent(File image, String tag, double durationSec)
            throws IOException, InterruptedException {

        File preparedImage = prepareImageForFfmpeg(image);
        File out = new File(workDir, "seg_" + tag + ".mp4");

        String filterComplex =
                "[0:v]scale=" + VIDEO_W + ":" + VIDEO_H + ":force_original_aspect_ratio=increase," +
                        "crop=" + VIDEO_W + ":" + VIDEO_H + ",boxblur=20:1[bg];" +
                        "[0:v]scale=" + VIDEO_W + ":" + VIDEO_H + ":force_original_aspect_ratio=decrease[fg];" +
                        "[bg][fg]overlay=(W-w)/2:(H-h)/2[v]";

        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-y");
        cmd.add("-loop");
        cmd.add("1");
        cmd.add("-t");
        cmd.add(String.valueOf(durationSec));
        cmd.add("-i");
        cmd.add(preparedImage.getAbsolutePath());
        cmd.add("-filter_complex");
        cmd.add(filterComplex);
        cmd.add("-map");
        cmd.add("[v]");
        cmd.add("-an");
        cmd.add("-c:v");
        cmd.add("libx264");
        cmd.add("-preset");
        cmd.add("fast");
        cmd.add("-crf");
        cmd.add("23");
        cmd.add("-r");
        cmd.add("30");
        cmd.add("-pix_fmt");
        cmd.add("yuv420p");
        cmd.add(out.getAbsolutePath());

        runFFmpeg(cmd);
        return out;
    }

    private File buildVideoSegmentSilent(MediaItem item, String tag)
            throws IOException, InterruptedException {

        File out = new File(workDir, "seg_" + tag + ".mp4");

        String transposeFilter = buildTransposeFilter(item.getRotation());

        String filterComplex =
                "[0:v]" + transposeFilter +
                        "scale=" + VIDEO_W + ":" + VIDEO_H + ":force_original_aspect_ratio=decrease," +
                        "pad=" + VIDEO_W + ":" + VIDEO_H + ":(ow-iw)/2:(oh-ih)/2:black[v]";

        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(item.getFile().getAbsolutePath());
        cmd.add("-filter_complex");
        cmd.add(filterComplex);
        cmd.add("-map");
        cmd.add("[v]");
        cmd.add("-an");
        cmd.add("-c:v");
        cmd.add("libx264");
        cmd.add("-preset");
        cmd.add("fast");
        cmd.add("-crf");
        cmd.add("23");
        cmd.add("-r");
        cmd.add("30");
        cmd.add("-pix_fmt");
        cmd.add("yuv420p");
        cmd.add(out.getAbsolutePath());

        runFFmpeg(cmd);
        return out;
    }

    private void concatenateSegments(List<File> segments, File output)
            throws IOException, InterruptedException {

        File concatList = new File(workDir, "concat_list.txt");

        try (PrintWriter pw = new PrintWriter(concatList)) {
            for (File seg : segments) {
                pw.println("file '" + seg.getAbsolutePath().replace("'", "'\\''") + "'");
            }
        }

        List<String> concatCmd = new ArrayList<>();
        concatCmd.add("ffmpeg");
        concatCmd.add("-y");
        concatCmd.add("-f");
        concatCmd.add("concat");
        concatCmd.add("-safe");
        concatCmd.add("0");
        concatCmd.add("-i");
        concatCmd.add(concatList.getAbsolutePath());
        concatCmd.add("-c");
        concatCmd.add("copy");
        concatCmd.add(output.getAbsolutePath());

        runFFmpeg(concatCmd);
    }

    private void addNarrationAndNormalize(File rawVideo, File narrationAudio, File output)
            throws IOException, InterruptedException {

        String filterComplex = "[1:a]apad,loudnorm=I=-14:TP=-1:LRA=10[a]";

        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(rawVideo.getAbsolutePath());
        cmd.add("-i");
        cmd.add(narrationAudio.getAbsolutePath());
        cmd.add("-filter_complex");
        cmd.add(filterComplex);
        cmd.add("-map");
        cmd.add("0:v");
        cmd.add("-map");
        cmd.add("[a]");
        cmd.add("-c:v");
        cmd.add("copy");
        cmd.add("-c:a");
        cmd.add("aac");
        cmd.add("-b:a");
        cmd.add("192k");
        cmd.add("-shortest");
        cmd.add(output.getAbsolutePath());

        runFFmpeg(cmd);
    }

    private File prepareImageForFfmpeg(File image) throws IOException, InterruptedException {
        String lower = image.getName().toLowerCase();

        if (!lower.endsWith(".heic")) {
            return image;
        }

        logger.accept("Converting HEIC image for FFmpeg: " + image.getName());

        String outputName = image.getName().replaceAll("(?i)\\.heic$", ".png");
        File converted = new File(workDir, outputName);

        List<String> cmd = new ArrayList<>();
        cmd.add("sips");
        cmd.add("-s");
        cmd.add("format");
        cmd.add("png");
        cmd.add(image.getAbsolutePath());
        cmd.add("--out");
        cmd.add(converted.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        int exit = p.waitFor();
        if (exit != 0 || !converted.exists()) {
            throw new IOException("Failed to convert HEIC to PNG:\n" + out);
        }

        return converted;
    }

    private void runFFmpeg(List<String> cmd) throws IOException, InterruptedException {
        logger.accept("FFmpeg: " + String.join(" ", cmd.subList(0, Math.min(6, cmd.size()))) + "...");

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        StringBuilder allOutput = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                allOutput.append(line).append("\n");
                if (line.contains("time=") || line.toLowerCase().contains("error")) {
                    logger.accept("[ffmpeg] " + line);
                }
            }
        }

        int exit = p.waitFor();
        if (exit != 0) {
            throw new IOException("FFmpeg exited with code " + exit +
                    " for command: " + String.join(" ", cmd) +
                    "\nOutput:\n" + allOutput);
        }
    }

    private String buildTransposeFilter(int rotation) {
        switch (rotation) {
            case 90:
                return "transpose=1,";
            case 180:
                return "transpose=2,transpose=2,";
            case 270:
                return "transpose=2,";
            default:
                return "";
        }
    }
}