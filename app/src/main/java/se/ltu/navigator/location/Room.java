package se.ltu.navigator.location;

import android.location.Location;

import se.ltu.navigator.navigation.Node;

public class Room extends Node {
    public Room(String id, double longitude, double latitude, int floor) {
        super(id, longitude, latitude, floor, Type.ROOM, null);
    }
}
