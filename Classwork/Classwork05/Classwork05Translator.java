import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Classwork05Translator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            String token = System.getenv("OpenAIToken");
            if (token == null || token.isBlank()) {
                System.out.println("Error: no se encontró la variable de entorno OpenAIToken.");
                return;
            }

            System.out.print("Enter the path of the .txt file: ");
            String inputPathStr = scanner.nextLine().trim();
            Path inputPath = Paths.get(inputPathStr);

            if (!Files.exists(inputPath)) {
                System.out.println("Error: file does not exist.");
                return;
            }

            String originalText = Files.readString(inputPath, StandardCharsets.UTF_8);

            System.out.print("Enter the language to translate to: ");
            String targetLanguage = scanner.nextLine().trim();

            String prompt = "Translate the following text to " + targetLanguage +
                    ". Return only the translated text:\n\n" + originalText;

            String escapedPrompt = escapeJson(prompt);

            String jsonBody = """
            {
              "model": "gpt-4.1-mini",
              "input": "%s"
            }
            """.formatted(escapedPrompt);

            ProcessBuilder pb = new ProcessBuilder(
                    "curl",
                    "https://api.openai.com/v1/responses",
                    "-H", "Content-Type: application/json",
                    "-H", "Authorization: Bearer " + token,
                    "-d", jsonBody
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.out.println("Error running curl.");
                System.out.println(response);
                return;
            }

            String apiResponse = response.toString();
            String translatedText = extractOutputText(apiResponse);

            if (translatedText == null || translatedText.isBlank()) {
                System.out.println("Could not extract translated text from API response.");
                System.out.println("Raw response:");
                System.out.println(apiResponse);
                return;
            }

            String outputFileName = buildOutputFileName(inputPath);
            Path outputPath = inputPath.getParent() != null
                    ? inputPath.getParent().resolve(outputFileName)
                    : Paths.get(outputFileName);

            Files.writeString(outputPath, translatedText, StandardCharsets.UTF_8);

            System.out.println("Translation completed successfully.");
            System.out.println("Output file: " + outputPath.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private static String buildOutputFileName(Path inputPath) {
        String fileName = inputPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName + "_translated.txt";
        }
        return fileName.substring(0, dotIndex) + "_translated.txt";
    }

    private static String extractOutputText(String json) {
        String key = "\"output_text\":\"";
        int start = json.indexOf(key);
        if (start == -1) return null;

        start += key.length();
        int end = start;
        StringBuilder result = new StringBuilder();

        while (end < json.length()) {
            char c = json.charAt(end);

            if (c == '"' && json.charAt(end - 1) != '\\') {
                break;
            }

            result.append(c);
            end++;
        }

        return result.toString()
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}