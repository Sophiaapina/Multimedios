package service;

import model.MediaItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class OpenAIService {

    private static final String CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String TTS_URL  = "https://api.openai.com/v1/audio/speech";

    private static final String TEXT_MODEL = "gpt-4o-mini";
    private static final String TTS_MODEL  = "gpt-4o-mini-tts";

    private final String apiKey;
    private final HttpClient http;

    public OpenAIService(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClient.newHttpClient();
    }

    public JSONObject generateAllContent(List<MediaItem> items,
                                         String firstPlace,
                                         String lastPlace) throws IOException, InterruptedException {

        StringBuilder mediaContext = new StringBuilder();
        for (MediaItem item : items) {
            mediaContext.append("- type: ").append(item.getType());

            if (item.getDateTime() != null) {
                mediaContext.append(", date: ").append(item.getDateTime());
            }

            if (item.hasGps()) {
                mediaContext.append(", latitude: ").append(item.getLatitude())
                        .append(", longitude: ").append(item.getLongitude());
            }

            mediaContext.append(", file: ").append(item.getFile().getName());
            mediaContext.append("\n");
        }

        String prompt = """
                You are creating the script for a travel video.

                The journey starts at %s and ends at %s.

                Based on the following media, generate:
                1. A cinematic narration in natural English (6 to 10 sentences total)
                2. One short inspirational phrase (max 20 words)

                Rules:
                - The narration should describe the journey as a whole
                - Do not mention file names
                - Do not mention GPS coordinates
                - Keep the tone warm, cinematic, and reflective
                - Return ONLY valid JSON
                - Use exactly this structure:

                {
                  "narration": "your narration here",
                  "phrase": "your phrase here"
                }

                Media:
                %s
                """.formatted(firstPlace, lastPlace, mediaContext);

        JSONObject body = new JSONObject();
        body.put("model", TEXT_MODEL);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "You are a helpful assistant that returns only valid JSON."));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt));

        body.put("messages", messages);
        body.put("temperature", 0.7);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CHAT_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("OpenAI text error " + response.statusCode() + ": " + response.body());
        }

        String text = new JSONObject(response.body())
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        text = text.replace("```json", "").replace("```", "").trim();

        return new JSONObject(text);
    }

    public void generateCoverImage(List<String> mediaDescriptions, File outputFile)
            throws IOException, InterruptedException {

        StringBuilder context = new StringBuilder();

        for (String desc : mediaDescriptions) {
            context.append(desc).append(", ");
        }

        String prompt = """
            A cinematic travel poster in portrait (9:16).
            Represent a journey through the following places and moments:
            %s

            Style:
            - modern, aesthetic, high quality
            - warm cinematic lighting
            - vibrant but elegant colors
            - travel photography collage feeling
            - no text, no words
            - ultra realistic
            """.formatted(context);

        JSONObject body = new JSONObject();
        body.put("model", "gpt-image-1");
        body.put("prompt", prompt);
        body.put("size", "1024x1024");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/images/generations"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("OpenAI Image error: " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        String base64 = json.getJSONArray("data")
                .getJSONObject(0)
                .getString("b64_json");

        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);

        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(imageBytes);
        }
    }
    public void textToSpeech(String text, File outputWav) throws IOException, InterruptedException {
        if (outputWav.getParentFile() != null && !outputWav.getParentFile().exists()) {
            outputWav.getParentFile().mkdirs();
        }

        JSONObject body = new JSONObject();
        body.put("model", TTS_MODEL);
        body.put("voice", "alloy");
        body.put("input", text);
        body.put("format", "wav");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TTS_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("OpenAI TTS error " + response.statusCode() + ": " + new String(response.body()));
        }

        try (FileOutputStream fos = new FileOutputStream(outputWav)) {
            fos.write(response.body());
        }
    }

}