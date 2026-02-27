package up.edu;

import up.edu.operations.ImageOperation;
import java.awt.image.BufferedImage;

public class ImageEditor {
    private BufferedImage image;

    public ImageEditor(BufferedImage image) { this.image = image; }

    public void applyOperation(ImageOperation op) { image = op.apply(image); }

    public BufferedImage getImage() { return image; }
}
