package model;
import java.io.File;
import java.time.LocalDateTime;


public class MediaItem {


    public enum Type { IMAGE, VIDEO }

    private final File file;
    private final Type type;
    private LocalDateTime dateTime;
    private double latitude;
    private double longitude;
    private int width;
    private int height;
    private int rotation;


    public MediaItem(File file, Type type) {
        this.file      = file;
        this.type      = type;
        this.latitude  = Double.NaN;
        this.longitude = Double.NaN;
        this.rotation  = 0;
    }

    public File          getFile()      { return file; }
    public Type          getType()      { return type; }
    public LocalDateTime getDateTime()  { return dateTime; }
    public double        getLatitude()  { return latitude; }
    public double        getLongitude() { return longitude; }
    public int           getWidth()     { return width; }
    public int           getHeight()    { return height; }
    public int           getRotation()  { return rotation; }


    public boolean hasGps() {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude);
    }

    public void setDateTime(LocalDateTime dateTime)   { this.dateTime  = dateTime;  }
    public void setLatitude(double latitude)          { this.latitude  = latitude;  }
    public void setLongitude(double longitude)        { this.longitude = longitude; }
    public void setWidth(int width)                   { this.width     = width;     }
    public void setHeight(int height)                 { this.height    = height;    }
    public void setRotation(int rotation)             { this.rotation  = rotation;  }

    @Override
    public String toString() {
        return String.format("MediaItem{file=%s, type=%s, date=%s, lat=%.4f, lon=%.4f}",
                file.getName(), type, dateTime, latitude, longitude);
    }
}