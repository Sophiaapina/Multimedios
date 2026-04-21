package service;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



public class GeocodingService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s";

    private final HttpClient http;

    public GeocodingService() {
        this.http = HttpClient.newHttpClient();
    }

    public String reverseGeocode(double latitude, double longitude) {
        try {

            String url = String.format(NOMINATIM_URL, latitude, longitude);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "GPS-Media-Video-Creator/1.0")
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return "Unknown location";

            JSONObject json = new JSONObject(resp.body());
            JSONObject addr = json.optJSONObject("address");
            if (addr == null) return json.optString("display_name", "Unknown location");

            for (String field : new String[]{
                    "city", "town", "village", "municipality",
                    "county", "state_district", "state", "country"}) {
                String val = addr.optString(field, "");
                if (!val.isBlank()) return val;
            }
            return json.optString("display_name", "Unknown location");

        } catch (Exception e) {
            System.err.println("[GeocodingService] Reverse geocode failed: " + e.getMessage());
            return "Unknown location";
        }
    }
}