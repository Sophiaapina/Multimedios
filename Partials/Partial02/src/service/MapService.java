package service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MapService {

    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1920;

    public void generateMapImage(double firstLat,
                                 double firstLon,
                                 double lastLat,
                                 double lastLon,
                                 String phrase,
                                 File outputFile) throws IOException {

        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g);

        Rectangle mapRect = new Rectangle(70, 220, 940, 1080);
        drawMapCard(g, mapRect);

        GeoBounds bounds = computeBounds(firstLat, firstLon, lastLat, lastLon);
        Point startPoint = mapLatLonToPoint(firstLat, firstLon, bounds, mapRect);
        Point endPoint = mapLatLonToPoint(lastLat, lastLon, bounds, mapRect);

        if (Math.abs(startPoint.x - endPoint.x) < 10 && Math.abs(startPoint.y - endPoint.y) < 10) {
            endPoint = new Point(
                    Math.min(mapRect.x + mapRect.width - 20, endPoint.x + 35),
                    Math.min(mapRect.y + mapRect.height - 20, endPoint.y + 35)
            );
        }

        drawGrid(g, mapRect);
        drawRouteLine(g, startPoint, endPoint);
        drawPin(g, startPoint.x, startPoint.y, new Color(0, 180, 120), "START");
        drawPin(g, endPoint.x, endPoint.y, new Color(220, 70, 70), "END");

        drawTitle(g);
        drawLegend(g, bounds);
        drawPhrase(g, phrase);

        g.dispose();

        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        ImageIO.write(img, "PNG", outputFile);
    }

    private void drawBackground(Graphics2D g) {
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(18, 30, 45),
                WIDTH, HEIGHT, new Color(45, 18, 38)
        );
        g.setPaint(gp);
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawMapCard(Graphics2D g, Rectangle r) {
        g.setColor(new Color(240, 244, 248));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 40, 40);

        g.setColor(new Color(170, 185, 200));
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 40, 40);
    }

    private void drawGrid(Graphics2D g, Rectangle r) {
        g.setColor(new Color(210, 220, 230));

        for (int i = 1; i < 6; i++) {
            int yy = r.y + i * (r.height / 6);
            g.drawLine(r.x + 25, yy, r.x + r.width - 25, yy);
        }

        for (int i = 1; i < 5; i++) {
            int xx = r.x + i * (r.width / 5);
            g.drawLine(xx, r.y + 25, xx, r.y + r.height - 25);
        }
    }

    private GeoBounds computeBounds(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == 0 && lon1 == 0) && (lat2 == 0 && lon2 == 0)) {
            return new GeoBounds(-90, 90, -180, 180);
        }

        double minLat = Math.min(lat1, lat2);
        double maxLat = Math.max(lat1, lat2);
        double minLon = Math.min(lon1, lon2);
        double maxLon = Math.max(lon1, lon2);

        double latSpan = Math.max(0.2, maxLat - minLat);
        double lonSpan = Math.max(0.2, maxLon - minLon);

        double latMargin;
        double lonMargin;

        if (latSpan < 0.5 && lonSpan < 0.5) {
            latMargin = 0.15;
            lonMargin = 0.15;
        } else if (latSpan < 2 && lonSpan < 2) {
            latMargin = latSpan * 0.25;
            lonMargin = lonSpan * 0.25;
        } else if (latSpan < 8 && lonSpan < 8) {
            latMargin = latSpan * 0.35;
            lonMargin = lonSpan * 0.35;
        } else {
            latMargin = latSpan * 0.50;
            lonMargin = lonSpan * 0.50;
        }

        minLat -= latMargin;
        maxLat += latMargin;
        minLon -= lonMargin;
        maxLon += lonMargin;

        minLat = Math.max(-90, minLat);
        maxLat = Math.min(90, maxLat);
        minLon = Math.max(-180, minLon);
        maxLon = Math.min(180, maxLon);

        return new GeoBounds(minLat, maxLat, minLon, maxLon);
    }

    private Point mapLatLonToPoint(double lat, double lon, GeoBounds bounds, Rectangle rect) {
        if (lat == 0 && lon == 0) {
            return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
        }

        double lonRange = bounds.maxLon - bounds.minLon;
        double latRange = bounds.maxLat - bounds.minLat;

        if (lonRange == 0) lonRange = 1;
        if (latRange == 0) latRange = 1;

        double nx = (lon - bounds.minLon) / lonRange;
        double ny = (bounds.maxLat - lat) / latRange;

        int px = rect.x + (int) (nx * rect.width);
        int py = rect.y + (int) (ny * rect.height);

        px = Math.max(rect.x + 25, Math.min(rect.x + rect.width - 25, px));
        py = Math.max(rect.y + 25, Math.min(rect.y + rect.height - 25, py));

        return new Point(px, py);
    }

    private void drawRouteLine(Graphics2D g, Point start, Point end) {
        g.setColor(new Color(255, 255, 255, 170));
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(start.x, start.y, end.x, end.y);
    }

    private void drawPin(Graphics2D g, int x, int y, Color color, String label) {
        int r = 22;

        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(x - r + 5, y - r + 7, r * 2, r * 2);

        g.setColor(color);
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(5f));
        g.draw(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));

        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(label);

        int boxX = x - textWidth / 2 - 14;
        int boxY = y - 72;
        int boxW = textWidth + 28;
        int boxH = 40;

        g.setColor(new Color(20, 20, 20, 210));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 18, 18);

        g.setColor(Color.WHITE);
        g.drawString(label, x - textWidth / 2, boxY + 28);
    }

    private void drawTitle(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 60));
        String title = "JOURNEY MAP";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 120);

        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        g.setColor(new Color(210, 220, 230));
        String subtitle = "First and last locations of the trip";
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(subtitle, (WIDTH - fm2.stringWidth(subtitle)) / 2, 165);
    }

    private void drawLegend(Graphics2D g, GeoBounds bounds) {
        int y = 1345;

        g.setFont(new Font("SansSerif", Font.BOLD, 28));

        g.setColor(new Color(0, 180, 120));
        g.fillOval(120, y - 20, 28, 28);
        g.setColor(Color.WHITE);
        g.drawString("Start", 165, y + 2);

        g.setColor(new Color(220, 70, 70));
        g.fillOval(320, y - 20, 28, 28);
        g.setColor(Color.WHITE);
        g.drawString("End", 365, y + 2);

        g.setFont(new Font("SansSerif", Font.PLAIN, 22));
        g.setColor(new Color(210, 220, 230));
        String zoomText = String.format("Zoomed area: %.2f° to %.2f° lat | %.2f° to %.2f° lon",
                bounds.minLat, bounds.maxLat, bounds.minLon, bounds.maxLon);
        g.drawString(zoomText, 120, y + 48);
    }

    private void drawPhrase(Graphics2D g, String phrase) {
        int boxX = 70;
        int boxY = 1460;
        int boxW = 940;
        int boxH = 320;

        g.setColor(new Color(255, 255, 255, 26));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 36, 36);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        g.drawString("Inspirational Phrase", boxX + 40, boxY + 60);

        g.setFont(new Font("Serif", Font.ITALIC, 40));
        g.setColor(new Color(240, 245, 250));

        drawWrappedCenteredText(g, phrase, boxX + 55, boxY + 125, boxW - 110, 54);
    }

    private void drawWrappedCenteredText(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int currentY = y;

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(testLine) > maxWidth) {
                String finalLine = line.toString();
                int drawX = x + (maxWidth - fm.stringWidth(finalLine)) / 2;
                g.drawString(finalLine, drawX, currentY);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (line.length() > 0) {
            String finalLine = line.toString();
            int drawX = x + (maxWidth - fm.stringWidth(finalLine)) / 2;
            g.drawString(finalLine, drawX, currentY);
        }
    }

    private static class GeoBounds {
        double minLat;
        double maxLat;
        double minLon;
        double maxLon;

        GeoBounds(double minLat, double maxLat, double minLon, double maxLon) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLon = minLon;
            this.maxLon = maxLon;
        }
    }
}