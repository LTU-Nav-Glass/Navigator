package se.ltu.navigator.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocationAPI {

    private List<Room> rooms;
    private Context context;
    private ExecutorService executorService;

    public LocationAPI(Context c) {
        context = c;
        rooms = loadLocations();
        executorService = Executors.newSingleThreadExecutor();
    }

    private List<Room> loadLocations() {
        List<Room> locationList = new ArrayList<>();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("locations.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                double longitude = jsonObject.getDouble("longitude");
                double latitude = jsonObject.getDouble("latitude");
                int floor = jsonObject.getInt("floor");
                Room location = new Room(id, longitude, latitude, floor);
                locationList.add(location);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return locationList;
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private void writeLocationToFile(String id, double longitude, double latitude, int floor) {
        try {
            // Read existing locations
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("locations.json");
            String jsonString = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            inputStream.close();

            // Parse JSON and add new location
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject newLocation = new JSONObject();
            newLocation.put("id", id);
            newLocation.put("longitude", longitude);
            newLocation.put("latitude", latitude);
            newLocation.put("floor", floor);
            jsonArray.put(newLocation);

            // Write updated JSON back to file
            try (FileOutputStream outputStream = context.openFileOutput("locations.json", Context.MODE_PRIVATE)) {
                outputStream.write(jsonArray.toString(4).getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    //method to get room object by id
    //only pulls local data because it assumes any required online calls have already been made
    public Room getRoomById(String id) {
        for (Room r : rooms) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    public void getLocationById(String locationId, Callback<Location> callback) {
        for (Room r : rooms) {
            if (r.getId().equals(locationId)) {
                callback.onResult(r.getLocation());
                return;
            }
        }

        executorService.submit(() -> {
            OkHttpClient client = new OkHttpClient();
            try {
                // Create URL with query parameters
                String urlString = String.format("https://map.ltu.se/api/data.json?l=LANG&q=%s&c=LUL", locationId);
                Request request = new Request.Builder()
                        .url(urlString)
                        .get()
                        .build();

//                System.out.println("Request URL: " + urlString);
                // Execute the request
                try (Response response = client.newCall(request).execute()) {
//                    System.out.println("Response code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();

                        // Parse JSON response
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray roomsArray = jsonObject.getJSONArray("rooms");
                        if (roomsArray.length() > 0) {
                            JSONObject roomObject = roomsArray.getJSONObject(0);
                            double latitude = roomObject.getDouble("y");
                            double longitude = roomObject.getDouble("x");
                            int floor = roomObject.getInt("level");
                            String id = roomObject.getString("name");

                            // Create and return new Room object
                            Room location = new Room(id, longitude, latitude, floor);
                            rooms.add(location);

                            // Write new location to locations.json
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                writeLocationToFile(id, longitude, latitude, floor);
                            }

                            callback.onResult(location.getLocation());
                        } else {
                            callback.onResult(null);
                        }
                    } else {
                        System.out.println("Error: " + response.code());
                        callback.onResult(null);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                callback.onResult(null);
            }
        });
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

    public interface Callback<T> {
        void onResult(T result);
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