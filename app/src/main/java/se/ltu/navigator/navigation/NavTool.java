package se.ltu.navigator.navigation;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import se.ltu.navigator.location.Room;

public class NavTool {

    private static final String[] GRAPH_FILES = { "graph_a.json" };
    private Graph[] graphs;
    private List<Node> path;

    public NavTool(Context context) {
        this.graphs = loadGraphs(context, GRAPH_FILES);
    }

    /**
     * Load the graphs from the given filenames.
     *
     * @param context The context of the application.
     * @param filenames The filenames of the graphs.
     * @return The loaded graphs.
     */
    public static Graph[] loadGraphs(Context context, String[] filenames) {
        Graph[] graphs = new Graph[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            graphs[i] = new Graph(context, filenames[i]);
        }
        return graphs;
    }

    /**
     * Function for finding the path a user should take to their destination. Calls the graph insertNodeAtClosestEdge
     * function to insert the destination into the graph. Then calls the graph findPath function to find the path.
     * @param longitude The longitude of the user.
     * @param latitude The latitude of the user.
     * @param room The room the user wants to go to.
     * last node is the destination.
     * TODO: Add Support for multiple graphs (ie buildings)
     */
    public void findPath(double longitude, double latitude, Room room) {
        //temporary check to only generate paths for A building
        Graph g = pickGraph(room.getId());
        if (g == null) {
            this.path = null;
            return;
        }
        List<Node> path = new ArrayList<>();
        g.insertNodeAtClosestEdge(room.getLocation().getLongitude(), room.getLocation().getLatitude(), room.getId(), room.getId()+"_temp");
        path = g.findShortestPath(longitude, latitude, room.getId());
        g.cleanGraph();
        this.path = path;

//        print out the path in order to debug
//        for (Node n : path) {
//            Log.d("NavTool", n.getId());
//        }
    }

    /**
     * Picks which graph to use based on the room id.
     * @param roomID The room id.
     * @return The graph.
     */
    private Graph pickGraph(String roomID) {
        switch (roomID.toLowerCase().charAt(0)) {
            case 'a':
                return graphs[0];
            default:
                return null;
        }
    }

    /**
     * Get the full path.
     * @return The path.
     */
    public List<Node> getPath() {
        return path;
    }

    /**
     * Get the next node in the path.
     * @return The next node.
     */
    public Node popFromPath() {
        if (!path.isEmpty()) {
            return path.remove(0);
        }
        return null;
    }

    /**
     * Get the next node in the path without removing it.
     * @return The next node.
     */
    public Node peekFromPath() {
        if (!path.isEmpty()) {
            return path.get(0);
        }
        return null;
    }
}
