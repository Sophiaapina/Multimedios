package up.edu.isgc;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Clock {

    public static void main(String[] args) throws Exception {

        int width = 400, height = 300;

        int hour = 10;
        int minute = 10;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Black background
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                img.setRGB(x, y, Color.BLACK.getRGB());

        int cx = width / 2;
        int cy = height / 2;
        int r = Math.min(width, height) / 2 - 20;

        // Circle
        int r2 = r * r;
        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                int dx = x - cx;
                int dy = y - cy;
                if (Math.abs(dx*dx + dy*dy - r2) < 200)
                    img.setRGB(x, y, Color.LIGHT_GRAY.getRGB());
            }
        }

        // Hour dots
        for (int i = 0; i < 12; i++) {
            double ang = Math.toRadians(i * 30 - 90);
            int px = cx + (int)((r - 15) * Math.cos(ang));
            int py = cy + (int)((r - 15) * Math.sin(ang));

            img.setRGB(px, py, Color.DARK_GRAY.getRGB());
        }

        // Hands
        int h = hour % 12;
        double hourAng = Math.toRadians((h * 30 + minute * 0.5) - 90);
        double minAng = Math.toRadians(minute * 6 - 90);

        drawLine(img, cx, cy,
                cx + (int)(r * 0.4 * Math.cos(hourAng)),
                cy + (int)(r * 0.4 * Math.sin(hourAng)),
                Color.LIGHT_GRAY.getRGB());

        drawLine(img, cx, cy,
                cx + (int)(r * 0.6 * Math.cos(minAng)),
                cy + (int)(r * 0.6 * Math.sin(minAng)),
                Color.LIGHT_GRAY.getRGB());

        ImageIO.write(img, "png", new File("clock.png"));
    }
    //line
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
