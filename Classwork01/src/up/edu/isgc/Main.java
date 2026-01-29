package up.edu.isgc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        int width = 400;
        int height = 300;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double slope = (double) height / width;

        for (int x = 0; x < width; x++) {
            double lineY = slope * x;

            for (int y = 0; y < height; y++) {
                if (y > lineY) {
                    image.setRGB(x, y, Color.BLUE.getRGB());
                } else {
                    image.setRGB(x, y, Color.RED.getRGB());
                }
            }
        }

            File outputImage = new File("Image.jpg");
            try {
                ImageIO.write(image, "jpg", outputImage);
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }
