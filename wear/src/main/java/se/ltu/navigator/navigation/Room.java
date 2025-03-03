package se.ltu.navigator.navigation;

public class Room extends Node {
    public Room(String id, double longitude, double latitude, int floor) {
        super(id, longitude, latitude, floor, Type.ROOM, null);
    }
}
