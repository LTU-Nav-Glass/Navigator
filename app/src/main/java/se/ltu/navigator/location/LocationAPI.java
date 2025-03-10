package se.ltu.navigator.location;

import android.content.Context;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationAPI {

    private List<Room> rooms;
    private Context context;
    private ExecutorService executorService;

    public LocationAPI(Context c) {
        context = c;
        rooms = loadRooms();
        executorService = Executors.newSingleThreadExecutor();
    }

    private List<Room> loadRooms() {
        File file = new File(context.getFilesDir(), "locations.json");

        List<Room> roomList = new ArrayList<>();
        try {
            InputStream inputStream;
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = context.getAssets().open("locations.json");
            }

            String jsonString = IOUtils.toString(inputStream);
            inputStream.close();

            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                double longitude = jsonObject.getDouble("longitude");
                double latitude = jsonObject.getDouble("latitude");
                int floor = jsonObject.getInt("floor");
                Room room = new Room(id, longitude, latitude, floor);
                roomList.add(room);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return roomList;
    }

    private void writeRoomToFile(Room room) {
        if (getRoomById(room.getId()) != null) {
            return; // Location already exists
        }
        rooms.add(room);
        try {
            // Read existing locations
            File file = new File(context.getFilesDir(), "locations.json");

            InputStream inputStream;
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = context.getAssets().open("locations.json");
            }

            String jsonString = IOUtils.toString(inputStream);
            inputStream.close();

            // Parse JSON and add new location
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject newRoom = new JSONObject();
            newRoom.put("id", room.getId());
            newRoom.put("longitude", room.getLocation().getLongitude());
            newRoom.put("latitude", room.getLocation().getLatitude());
            newRoom.put("floor", room.getFloor());
            jsonArray.put(newRoom);

            // Write updated JSON back to file
            try (FileOutputStream outputStream = context.openFileOutput("locations.json", Context.MODE_PRIVATE)) {
                outputStream.write(jsonArray.toString(4).getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Should only be used when certain that room has already been fetched to local database
     * @param id The room ID.
     * @return The room pulled from local storage (it doesn't fetch information online).
     */
    public Room getRoomById(String id) {
        for (Room r : rooms) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    /**
     * @param roomId The room ID.
     * @param callback Callback invoked with location object fetched from the LTU Map database.
     */
    public void getLocationById(String roomId, Callback<Location> callback) {
        getRoomById(roomId, room -> {
            if (room != null) {
                callback.onResult(room.getLocation());
            } else {
                callback.onResult(null);
            }
        });
    }

    /**
     * @param roomId The room ID.
     * @param callback Callback invoked with room object fetched from the LTU Map database.
     */
    public void getRoomById(String roomId, Callback<Room> callback) {
        for (Room r : rooms) {
            if (r.getId().equals(roomId)) {
                callback.onResult(r);
                return;
            }
        }

        executorService.submit(() -> {
            OkHttpClient client = new OkHttpClient();
            try {
                // Create URL with query parameters
                String urlString = String.format("https://map.ltu.se/api/data.json?l=LANG&q=%s&c=LUL", roomId);
                Request request = new Request.Builder()
                        .url(urlString)
                        .get()
                        .build();

                // Execute the request
                try (Response response = client.newCall(request).execute()) {
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
                            Room room = new Room(id, longitude, latitude, floor);

                            // Write new room to locations.json
                            writeRoomToFile(room);

                            callback.onResult(room);
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

    // Not important, but this is a duplicate of the Java Standard Library's Supplier
    // @see java.util.function.Supplier
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