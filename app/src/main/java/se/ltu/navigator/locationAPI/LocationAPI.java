package se.ltu.navigator.locationAPI;

import android.content.Context;
import android.content.res.AssetManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocationAPI {

    private List<Room> rooms;

    public LocationAPI(Context context) {
        rooms = loadLocations(context);
    }

    private List<Room> loadLocations(Context context) {
        List<Room> locationList = new ArrayList<>();
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
                double longitude = jsonObject.getDouble("longitude");
                double latitude = jsonObject.getDouble("latitude");
                Room location = new Room(id, longitude, latitude);
                locationList.add(location);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return locationList;
    }

    public Location getLocationById(String locationId) {
        for (Room r : rooms) {
            if (r.getId().equals(locationId)) {
                return r.getLocation();
            }
        }
        return null; // Location not found
    }

    public List<String> findLocationsByPartialId(String partialId) {
        List<String> matchingRooms = new ArrayList<>();
        if (partialId == null || partialId.trim().isEmpty()) {
            return matchingRooms; // Return empty list for null or empty input
        }

        String lowerCasePartialId = partialId.toLowerCase();
        for (Room r : rooms) {
            if (r.getId().toLowerCase().contains(lowerCasePartialId)) {
                matchingRooms.add(r.getId());
            }
        }
        return matchingRooms;
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