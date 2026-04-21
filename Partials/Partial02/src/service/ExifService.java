package service;


import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.mp4.Mp4Directory;

import model.MediaItem;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class ExifService {


    public void populate(MediaItem item) {
        File file = item.getFile();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            extractDate(item, metadata);
            extractGps(item, metadata);
            extractDimensions(item, metadata);
            extractOrientation(item, metadata);
        } catch (Exception e) {
            System.err.println("[ExifService] Could not read metadata for: "
                    + file.getName() + " — " + e.getMessage());
        }
    }


    private void extractDate(MediaItem item, Metadata metadata) {
        Date date = null;

        ExifSubIFDDirectory subIfd = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (subIfd != null) {
            date = subIfd.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        }
        if (date == null) {
            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0 != null) {
                date = ifd0.getDate(ExifIFD0Directory.TAG_DATETIME);
            }
        }
        if (date == null) {
            Mp4Directory mp4 = metadata.getFirstDirectoryOfType(Mp4Directory.class);
            if (mp4 != null) {
                date = mp4.getDate(Mp4Directory.TAG_CREATION_TIME);
            }
        }

        if (date != null) {
            item.setDateTime(date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        } else {
            item.setDateTime(LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(item.getFile().lastModified()),
                    ZoneId.systemDefault()));
        }
    }

    private void extractGps(MediaItem item, Metadata metadata) {
        GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDir == null) return;

        com.drew.lang.GeoLocation geo = gpsDir.getGeoLocation();
        if (geo != null && !geo.isZero()) {
            item.setLatitude(geo.getLatitude());
            item.setLongitude(geo.getLongitude());
        }
    }

    private void extractDimensions(MediaItem item, Metadata metadata) {
        for (Directory dir : metadata.getDirectories()) {
            int w = safeInt(dir, 0x0100);
            int h = safeInt(dir, 0x0101);
            if (w > 0 && h > 0) {
                item.setWidth(w);
                item.setHeight(h);
                return;
            }
            w = safeInt(dir, 0xA002);
            h = safeInt(dir, 0xA003);
            if (w > 0 && h > 0) {
                item.setWidth(w);
                item.setHeight(h);
                return;
            }
        }
    }

    private void extractOrientation(MediaItem item, Metadata metadata) {
        ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (ifd0 == null) return;
        int orientation = ifd0.getInteger(ExifIFD0Directory.TAG_ORIENTATION) != null
                ? ifd0.getInteger(ExifIFD0Directory.TAG_ORIENTATION) : 1;
        item.setRotation(orientationToDegrees(orientation));
    }

    private int orientationToDegrees(int orientation) {
        return switch (orientation) {
            case 3  -> 180;
            case 6  -> 90;
            case 8  -> 270;
            default -> 0;
        };
    }

    private int safeInt(Directory dir, int tag) {
        try {
            Integer val = dir.getInteger(tag);
            return val != null ? val : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}