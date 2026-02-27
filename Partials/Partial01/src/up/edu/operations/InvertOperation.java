package up.edu.operations;

import up.edu.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class InvertOperation implements ImageOperation {

    private Rectangle region;

    public InvertOperation(Rectangle region) {
        this.region = region;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {

        for (int i = region.x; i < region.x + region.width; i++) {
            for (int j = region.y; j < region.y + region.height; j++) {

                if (i < image.getWidth() && j < image.getHeight()) {

                    Color color = new Color(image.getRGB(i, j));

                    int r = 255 - color.getRed();
                    int g = 255 - color.getGreen();
                    int b = 255 - color.getBlue();

                    Color inverted = new Color(r, g, b);
                    image.setRGB(i, j, inverted.getRGB());
                }
            }
        }

        return image;
    }
}
