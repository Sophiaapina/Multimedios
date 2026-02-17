package Classwork.Classwork03;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class svg {
    public static void main(String[] args) throws Exception {
        int width = 600;
        int height = 450;

        String svg =
            "<svg width=\"" + width + "\" height=\"" + height + "\" viewBox=\"0 0 " + width + " " + height + "\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
            "  <polygon points=\"0,0 " + width + ",0 " + width + "," + height + "\" fill=\"#E11B1B\"/>\n" +
            "  <polygon points=\"0,0 0," + height + " " + width + "," + height + "\" fill=\"#0B2BFF\"/>\n" +
            "</svg>\n";

        Files.write(Path.of("triangulos.svg"), svg.getBytes(StandardCharsets.UTF_8));
        System.out.println("Listo: triangulos.svg");
    }
}
