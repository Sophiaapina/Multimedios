package up.edu.operations;

import java.awt.image.BufferedImage;

public class CropOperation implements ImageOperation {

    private int x1, y1, x2, y2;

    public CropOperation(int x1, int y1, int x2, int y2) {
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = x2 - x1;
        int height = y2 - y1;

        return image.getSubimage(x1, y1, width, height);
    }
}
