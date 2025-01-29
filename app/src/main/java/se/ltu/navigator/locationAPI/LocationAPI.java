package se.ltu.navigator.locationAPI;

import android.content.Context;
import android.content.res.AssetManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocationAPI {

    private List<Location> locations;

    public LocationAPI(Context context) {
        locations = loadLocations(context);
    }

    private List<Location> loadLocations(Context context) {
        List<Location> locationList = new ArrayList<>();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("locations.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String jsonString = new String(buffer, "UTF-8");

            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                float longitude = (float) jsonObject.getDouble("longitude");
                float latitude = (float) jsonObject.getDouble("latitude");
                Location location = new Location(id, longitude, latitude);
                locationList.add(location);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return locationList;
    }

    public Location getLocationById(String locationId) {
        for (Location location : locations) {
            if (location.getId().equals(locationId)) {
                return location;
            }
        }
        return null; // Location not found
    }
}

/* *** DEMO USAGE ***

locationApi = new LocationApi(this); // Initialize the API with the context

Location location = locationApi.getLocationById("test");
if (location != null) {
    System.out.println("Found location: " + location.getId() + ", Longitude: " + location.getLongitude() + ", Latitude: " + location.getLatitude());
} else {
    System.out.println("Location not found.");
}

 */