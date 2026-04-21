package service;

import model.MediaItem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;

public class MapService {

    public File generateMap(List<MediaItem> items, File outputFile) throws Exception {

        MediaItem first = null;
        MediaItem last = null;

        for (MediaItem item : items) {
            if (!Double.isNaN(item.getLatitude()) && !Double.isNaN(item.getLongitude())) {
                if (first == null) first = item;
                last = item;
            }
        }

        if (first == null || last == null) {
            throw new RuntimeException("No GPS data found.");
        }

        double lat1 = first.getLatitude();
        double lon1 = first.getLongitude();
        double lat2 = last.getLatitude();
        double lon2 = last.getLongitude();

        double centerLat = (lat1 + lat2) / 2.0;
        double centerLon = (lon1 + lon2) / 2.0;

        double distance = Math.max(Math.abs(lat1 - lat2), Math.abs(lon1 - lon2));

        int zoom;
        if (distance < 0.1) zoom = 14;
        else if (distance < 0.5) zoom = 12;
        else if (distance < 1) zoom = 10;
        else if (distance < 5) zoom = 7;
        else if (distance < 10) zoom = 5;
        else zoom = 3;

        String url = String.format(
                "https://staticmap.openstreetmap.de/staticmap.php?center=%f,%f&zoom=%d&size=800x1200&markers=%f,%f,green-pushpin|%f,%f,red-pushpin",
                centerLat, centerLon, zoom,
                lat1, lon1,
                lat2, lon2
        );

        System.out.println("🗺️ Map URL: " + url);

        BufferedImage mapImage = ImageIO.read(new URL(url));

        BufferedImage finalImage = new BufferedImage(
                mapImage.getWidth(),
                mapImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = finalImage.createGraphics();

        g.drawImage(mapImage, 0, 0, null);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(20, 20, 400, 60, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("Your Journey", 40, 60);

        g.dispose();

        ImageIO.write(finalImage, "png", outputFile);

        return outputFile;
    }
}