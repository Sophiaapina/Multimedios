package up.edu;

import up.edu.operations.*;
import java.awt.image.BufferedImage;

public class Main {

    public static void main(String[] args) {

        try {
            System.out.println(new java.io.File("resources/input.png").getAbsolutePath());
            ImageLoader loader = new ImageLoader();
            BufferedImage img = loader.load("/Users/sophiapina/Desktop/Multimedios/Partials/resources/input.png");


            ImageEditor editor = new ImageEditor(img);

            editor.applyOperation(new CropOperation(10, 10, 400, 300));
            editor.applyOperation(new InvertOperation(new Rectangle(50, 50, 100, 100)));
            editor.applyOperation(new RotateOperation(new Rectangle(60, 60, 150, 150), 90));

            loader.save(editor.getImage(), "output.png");

            System.out.println("Image processed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
