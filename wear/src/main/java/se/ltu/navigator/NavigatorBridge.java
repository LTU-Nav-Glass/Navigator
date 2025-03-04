package se.ltu.navigator;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
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
import java.util.Objects;

import se.ltu.navigator.navigation.Room;

public class NavigatorBridge implements DataClient.OnDataChangedListener {

    public static final String CURRENT_LOCATION_KEY = "se.ltu.navigator.key.current_location";
    public static final String CURRENT_FLOOR_KEY = "se.ltu.navigator.key.current_floor";
    public static final String TARGET_ROOM_KEY = "se.ltu.navigator.key.target_room";

    private final Gson gson;

    private Location currentLocation;
    private int currentFloor;
    private Room destinationRoom;

    public NavigatorBridge() {
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

        currentFloor = Integer.MIN_VALUE;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public Room getDestinationRoom() {
        return destinationRoom;
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                Log.i("NavigatorBridge", "Detecting data change in '" + item.getUri().getPath() + "'");

                if (item.getUri().getPath() != null) {
                    switch (item.getUri().getPath().split("/")[1]) {
                        case "location":
                            if (Objects.equals(dataMap.getString(CURRENT_LOCATION_KEY), "null")) {
                                currentLocation = null;
                            } else {
                                currentLocation = gson.fromJson(
                                        dataMap.getString(CURRENT_LOCATION_KEY),
                                        Location.class
                                );
                            }
                            break;
                        case "floor":
                            currentFloor = dataMap.getInt(CURRENT_FLOOR_KEY);
                            break;
                        case "room":
                            if (Objects.equals(dataMap.getString(TARGET_ROOM_KEY), "null")) {
                                destinationRoom = null;
                            } else {
                                destinationRoom = gson.fromJson(
                                        dataMap.getString(TARGET_ROOM_KEY),
                                        Room.class
                                );
                            }
                            break;
                    }
                }
            }
        }
    }
}
