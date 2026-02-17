package Classwork.Classwork03;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class scene {
    public static void main(String[] args) throws Exception {

        int width = 400;
        int height = 300;

        String svg =
        "<svg width=\"" + width + "\" height=\"" + height + "\" viewBox=\"0 0 " + width + " " + height + "\" xmlns=\"http://www.w3.org/2000/svg\">\n" +

        "  <rect width=\"100%\" height=\"100%\" fill=\"white\" />\n" +

        "  <circle cx=\"100\" cy=\"90\" r=\"45\" fill=\"yellow\" />\n" +

        "  <g stroke=\"#CC5555\" stroke-width=\"2\">\n" +
        "    <line x1=\"100\" y1=\"20\" x2=\"100\" y2=\"0\" />\n" +
        "    <line x1=\"100\" y1=\"160\" x2=\"100\" y2=\"180\" />\n" +
        "    <line x1=\"20\" y1=\"90\" x2=\"0\" y2=\"90\" />\n" +
        "    <line x1=\"180\" y1=\"90\" x2=\"160\" y2=\"90\" />\n" +
        "    <line x1=\"40\" y1=\"30\" x2=\"25\" y2=\"15\" />\n" +
        "    <line x1=\"160\" y1=\"150\" x2=\"175\" y2=\"165\" />\n" +
        "    <line x1=\"160\" y1=\"30\" x2=\"175\" y2=\"15\" />\n" +
        "    <line x1=\"40\" y1=\"150\" x2=\"25\" y2=\"165\" />\n" +
        "  </g>\n" +

        "  <path d=\"\n" +
        "    M 0 320\n" +
        "    Q 50 280 100 320\n" +
        "    T 200 320\n" +
        "    T 300 320\n" +
        "    T 400 320\n" +
        "    T 500 320\n" +
        "    T 600 320\n" +
        "    L 600 450\n" +
        "    L 0 450\n" +
        "    Z\" fill=\"#00FF00\" />\n" +

        "</svg>";

        Files.write(Path.of("scene.svg"), svg.getBytes(StandardCharsets.UTF_8));
    }
}
