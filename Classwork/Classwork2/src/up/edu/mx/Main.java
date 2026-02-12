package up.edu.mx;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        drawBarycentricRGB();
    }

    public static void drawBarycentricRGB() {

        BufferedImage image = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, Color.BLACK.getRGB());
            }
        }

        double xA = image.getWidth() * 0.2;
        double yA = image.getHeight() * 0.9;

        double xB = image.getWidth() * 0.8;
        double yB = image.getHeight() * 0.9;

        double xC = image.getWidth() * 0.5;
        double yC = image.getHeight() * 0.15;

        // Denominador
        double denom = (yB - yC) * (xA - xC) + (xC - xB) * (yA - yC);


        int minX = (int) Math.floor(Math.min(xA, Math.min(xB, xC)));
        int maxX = (int) Math.ceil (Math.max(xA, Math.max(xB, xC)));
        int minY = (int) Math.floor(Math.min(yA, Math.min(yB, yC)));
        int maxY = (int) Math.ceil (Math.max(yA, Math.max(yB, yC)));


        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        maxX = Math.min(image.getWidth() - 1, maxX);
        maxY = Math.min(image.getHeight() - 1, maxY);


        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                double l1 = ((yB - yC) * (x - xC) + (xC - xB) * (y - yC)) / denom;
                double l2 = ((yC - yA) * (x - xC) + (xA - xC) * (y - yC)) / denom;
                double l3 = 1.0 - l1 - l2;

                if (l1 >= 0 && l2 >= 0 && l3 >= 0) {
                    int r = clamp255((int) Math.round(255 * l1)); // rojo
                    int g = clamp255((int) Math.round(255 * l2)); // verde
                    int b = clamp255((int) Math.round(255 * l3)); // azul

                    image.setRGB(x, y, new Color(r, g, b).getRGB());
                }
            }
        }

        saveImage(image, "TriangleRGB", "png");
    }

    private static int clamp255(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    public static void saveImage(BufferedImage image, String fileName, String fileType) {
        File file = new File(fileName + "." + fileType);
        try {
            ImageIO.write(image, fileType, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
