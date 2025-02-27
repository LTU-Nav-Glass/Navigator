package se.ltu.navigator;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.wear.remote.interactions.RemoteActivityHelper;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import se.ltu.navigator.location.Room;

public class WatchBridge {

    public static final String CURRENT_LOCATION_KEY = "se.ltu.navigator.key.current_location";
    public static final String CURRENT_FLOOR_KEY = "se.ltu.navigator.key.current_floor";
    public static final String TARGET_ROOM_KEY = "se.ltu.navigator.key.target_room";

    private final DataClient dataClient;
    private final Gson gson;
    private final RemoteActivityHelper helper;
    private final Uri appURI;
    private final Intent intent;

    public WatchBridge(MainActivity mainActivity) {
        dataClient = Wearable.getDataClient(mainActivity);

        helper = new RemoteActivityHelper(mainActivity);
        appURI = Uri.parse("navigator://mainactivity");
        intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setData(appURI);

        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(Location.class, new JsonSerializer<Location>() {
            @Override
            public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject json = new JsonObject();

                json.addProperty("longitude", src.getLongitude());
                json.addProperty("latitude", src.getLatitude());

                return json;
            }
        });

        gsonBuilder.registerTypeAdapter(Location.class, new JsonDeserializer<Location>() {
            @Override
            public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject obj = json.getAsJsonObject();
                Location location = new Location("");

                location.setLongitude(obj.getAsJsonPrimitive("longitude").getAsDouble());
                location.setLatitude(obj.getAsJsonPrimitive("latitude").getAsDouble());
                location.setAccuracy(0);

                return location;
            }
        });

        gson = gsonBuilder.serializeNulls().create();
    }

    private Task<DataItem> sendData(PutDataMapRequest putDataMapRequest) {
        Log.i("WatchBridge", "Sending data information to wearable");
        putDataMapRequest.setUrgent();
        Task<DataItem> task = dataClient.putDataItem(putDataMapRequest.asPutDataRequest());

        task.addOnCanceledListener(() -> Log.e("WatchBridge", "Data transfer cancelled"));
        task.addOnFailureListener((e) -> Log.e("WatchBridge", "Data transfer failed: " + e.getMessage()));
        task.addOnSuccessListener((i) -> Log.i("WatchBridge", "Data transfer successfully completed"));

        return task;
    }

    public void setCurrentLocation(Location location) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/location");
        if (location == null) {
            putDataMapReq.getDataMap().putString(CURRENT_LOCATION_KEY, "null");
        } else {
            putDataMapReq.getDataMap().putString(CURRENT_LOCATION_KEY, gson.toJson(location));
        }
        sendData(putDataMapReq);
    }

    public void setCurrentFloor(int floor) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/floor");
        putDataMapReq.getDataMap().putInt(CURRENT_FLOOR_KEY, floor);
        sendData(putDataMapReq);
    }

    public void setTargetRoom(Room room) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/room");
        if (room == null) {
            putDataMapReq.getDataMap().putString(TARGET_ROOM_KEY, "null");
        } else {
            putDataMapReq.getDataMap().putString(TARGET_ROOM_KEY, gson.toJson(room));
        }
        sendData(putDataMapReq);
    }

    public void startNavigation() {
        helper.startRemoteActivity(intent, appURI.getHost());
    }
}
