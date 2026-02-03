package up.edu.isgc;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Drawing {

    public static void main(String[] args) throws Exception {

        int width = 400;
        int height = 300;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        //sky
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                img.setRGB(x, y, Color.WHITE.getRGB());

        int sunX = 80;
        int sunY = 80;
        int sunR = 35;

        // Sunrays
        for (int i = 0; i < 8; i++) {

            double ang = Math.toRadians(i * 45);

            int x2 = sunX + (int)(60 * Math.cos(ang));
            int y2 = sunY + (int)(60 * Math.sin(ang));

            drawLine(img, sunX, sunY, x2, y2, new Color(200,100,100).getRGB());
        }

        // sun

        for (int x = sunX - sunR; x <= sunX + sunR; x++) {
            for (int y = sunY - sunR; y <= sunY + sunR; y++) {

                int dx = x - sunX;
                int dy = y - sunY;

                if (dx*dx + dy*dy <= sunR*sunR)
                    img.setRGB(x, y, Color.YELLOW.getRGB());
            }
        }


        // Grass
        int base = 220;
        double amp = 15;
        double freq = 0.08;

        for (int x = 0; x < width; x++) {

            int yWave = (int)(base + amp * Math.sin(freq * x));

            for (int y = yWave; y < height; y++) {
                img.setRGB(x, y, Color.GREEN.getRGB());
            }
        }

        ImageIO.write(img, "png", new File("Drawing.png"));
    }

    //Line
    static void drawLine(BufferedImage img, int x1, int y1, int x2, int y2, int rgb) {

        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        double x = x1;
        double y = y1;

        double xInc = dx / (double) steps;
        double yInc = dy / (double) steps;

        for (int i = 0; i <= steps; i++) {

            int xi = (int)Math.round(x);
            int yi = (int)Math.round(y);

            if (xi >= 0 && xi < img.getWidth() && yi >= 0 && yi < img.getHeight())
                img.setRGB(xi, yi, rgb);

            x += xInc;
            y += yInc;
        }
    }
}
