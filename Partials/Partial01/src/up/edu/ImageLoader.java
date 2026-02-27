package up.edu;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageLoader {

    public BufferedImage load(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public void save(BufferedImage image, String path) throws IOException {
        ImageIO.write(image, "png", new File(path));
    }
}
