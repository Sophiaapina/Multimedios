package up.edu.operations;

import up.edu.Rectangle;
import java.awt.image.BufferedImage;

public class RotateOperation implements ImageOperation {

    private Rectangle region;
    private int degrees;

    public RotateOperation(Rectangle region, int degrees) {
        if (degrees != 90 && degrees != 180 && degrees != 270) {
            throw new IllegalArgumentException("Rotation must be 90, 180, or 270 degrees.");
        }
        this.region = region;
        this.degrees = degrees;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {

        BufferedImage copy = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                image.getType()
        );

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                copy.setRGB(i, j, image.getRGB(i, j));
            }
        }

        for (int i = 0; i < region.width; i++) {
            for (int j = 0; j < region.height; j++) {

                int px = region.x + i;
                int py = region.y + j;

                if (px < image.getWidth() && py < image.getHeight()) {

                    int rgb = image.getRGB(px, py);

                    int newX = px;
                    int newY = py;

                    switch (degrees) {
                        case 90:
                            newX = region.x + region.height - j - 1;
                            newY = region.y + i;
                            break;
                        case 180:
                            newX = region.x + region.width - i - 1;
                            newY = region.y + region.height - j - 1;
                            break;
                        case 270:
                            newX = region.x + j;
                            newY = region.y + region.width - i - 1;
                            break;
                    }

                    if (newX < image.getWidth() && newY < image.getHeight()) {
                        copy.setRGB(newX, newY, rgb);
                    }
                }
            }
        }

        return copy;
    }
}
