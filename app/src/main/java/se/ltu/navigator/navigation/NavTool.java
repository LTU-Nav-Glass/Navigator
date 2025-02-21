package se.ltu.navigator.navigation;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import se.ltu.navigator.location.Room;

public class NavTool {

    private static final String[] GRAPH_FILES = { "graph_a.json" };
    private Graph[] graphs;

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
     * @return The path the user should take. The first node is the user's current location and the
     * last node is the destination.
     * TODO: Add Support for multiple graphs (ie buildings)
     */
    public List<Node> findPath(double longitude, double latitude, Room room) {
        List<Node> path = new ArrayList<>();
        for (Graph graph : graphs) {
            graph.insertNodeAtClosestEdge(room.getLocation().getLongitude(), room.getLocation().getLatitude(), room.getId(), room.getId()+"_temp");
            path = graph.findShortestPath(longitude, latitude, room.getId());
        }
        for (Graph graph : graphs) {
            graph.cleanGraph();
        }
        return path;
    }
}
