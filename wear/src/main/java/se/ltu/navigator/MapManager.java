package se.ltu.navigator;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MapManager {
    private static final String TAG = "MapManager";
    private final String LTU_MAP_FILENAME = "planet_22.13,65.615_22.151,65.621.map";
    private CompassManager compassManager;
    private MainActivity mainActivity;
    private AssetManager assetManager;
    protected MapView mapView;
    private ArrayList<ArrayList<String>> asset_filenames; // Holds array of building map filenames
    private double[][] building_bounds;
    private int current_building_index;
    private String currentFilename;

    public MapManager(MainActivity mainActivity, CompassManager compassManager) {
        this.mainActivity = mainActivity;
        this.compassManager = compassManager;

        assetManager = mainActivity.getAssets();
        asset_filenames = new ArrayList<>();
        building_bounds = new double[5][4 * 2]; // 5 buildings each with 4 corners with latitude and longitude
        initMapList();
        initBuildingBoundaries();

        currentFilename = LTU_MAP_FILENAME; //set to LTU map first
        current_building_index = -1; //-1 indicates user is outside
    }

    /**
     * This method measures the user's coordinates and displays the correct map
     */
    public void switchMap() {
        current_building_index = 0; // Todo
        String lastFilename = currentFilename;

        if (current_building_index == -1)
            currentFilename = LTU_MAP_FILENAME;
        else {
            // Check that user's current floor works
            currentFilename = asset_filenames.get(current_building_index).get(1); // todo
//            Log.d(TAG, currentFilename);
        }
        if (lastFilename.compareTo(currentFilename) != 0)
            mapSetupHandler();
    }

    // ISSUE, DOES NOT SWITCH BACK TO COMPASS
    public void mapSetupHandler() {
        mapLTUSetup();
        if (this.current_building_index > -1) {
            mapPDFSetup();
        }
    }

    /**
     * This method switches the current map displayed on app when user changes floors TODO
     */
    public void switchCurrentFloor(int floorDir) {

        String building = currentFilename.substring(0, 1);
        int current_filename_index = asset_filenames.indexOf(currentFilename);

        if (building.compareTo("A") == 0) {
            try {
                currentFilename = asset_filenames.get(current_building_index).get(current_filename_index + floorDir);
            }
            catch (IndexOutOfBoundsException e) {

            }
        } else if (building.compareTo("B") == 0) // No maps available for other buildings but is here for transparency for implementing in the future
        {

        }
    }

    public MapView getMapView() {
        return mapView;
    }

    private void mapLTUSetup() {
        try {
            mapView = mainActivity.findViewById(R.id.mapView);

            mapView.setClickable(false);
            mapView.getMapScaleBar().setVisible(false);
            mapView.setBuiltInZoomControls(false);

            InputStream inputStream = assetManager.open(LTU_MAP_FILENAME);

            File tempFile = File.createTempFile("temp_map", ".map", mainActivity.getCacheDir());

            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int fileLength = inputStream.read(buffer);

            while ((fileLength > 0)) {
                outputStream.write(buffer, 0, fileLength);
                fileLength = inputStream.read(buffer);
            }

            outputStream.close();
            inputStream.close();


            TileCache tileCache = AndroidUtil.createTileCache(mainActivity, "mapcache",
                    mapView.getModel().displayModel.getTileSize(), 1f,
                    mapView.getModel().frameBufferModel.getOverdrawFactor());


            MapFile mapData = new MapFile(tempFile);

            TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache,
                    mapData, mapView.getModel().mapViewPosition,
                    AndroidGraphicFactory.INSTANCE);

            File renderThemeFile = new File(mainActivity.getCacheDir(), "default.xml");
            try (InputStream renderThemeStream = assetManager.open("default.xml");
                 FileOutputStream renderThemeOutputStream = new FileOutputStream(renderThemeFile)) {
                while ((fileLength = renderThemeStream.read(buffer)) > 0) {
                    renderThemeOutputStream.write(buffer, 0, fileLength);
                }
            }

            tileRendererLayer.setXmlRenderTheme(new ExternalRenderTheme(renderThemeFile));


            mapView.getLayerManager().getLayers().add(tileRendererLayer);
            mapView.setCenter(new LatLong(65.618, 22.141)); // initial coordinates when current location isn´t yet available
            mapView.setZoomLevel((byte) 18); //18

//            enableInteraction();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void enableInteraction() {
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(false);
        mapView.setBuiltInZoomControls(true);
        mapView.getMapZoomControls().hide();
    }

    private void mapPDFSetup() {
        try {
            Bitmap initbitmap = BitmapFactory.decodeStream(assetManager.open(this.currentFilename));

            Matrix matrix = new Matrix();
            matrix.postScale(0.8f, 0.8f); // Scale
            matrix.postRotate(-21); // Rotate

            // Create a new transformed Bitmap
            Bitmap transformedBitmap = Bitmap.createBitmap(initbitmap, 0, 0, initbitmap.getWidth(), initbitmap.getHeight(), matrix, true);

            Drawable drawable = new BitmapDrawable(null, transformedBitmap);
            org.mapsforge.core.graphics.Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
            LatLong targetLatLong = new LatLong(65.61716, 22.13814); // map needs to be centered here to be displayed correctly - only tested for floor 2
            Marker targetMarker = new Marker(targetLatLong, bitmap, 0, 0);
            mapView.getLayerManager().getLayers().add(targetMarker);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initMapList() {
        // A Hus
        ArrayList<String> a_Hus = new ArrayList();
        a_Hus.add("A-huset1.png");
        a_Hus.add("A-huset2.png");
        a_Hus.add("A-huset3.png");

        // B Hus
        ArrayList<String> b_Hus = new ArrayList<>();

        // ...etc

        asset_filenames.add(a_Hus);
        asset_filenames.add(b_Hus);
    }

    /**
     * This method initializes the boundaries of each building to measure if the user is in the general area
     * This simplifies each building to 4 corners and creates a square area around (even though the buildings are more complex shapes)
     * The even indexes are latitude and the odd are longitude
     */
    private void initBuildingBoundaries() {
        // A Hus
        //NW Corner
        building_bounds[0][0] = 65.61736064369937;
        building_bounds[0][1] = 22.13577732432116;
        //NE Corner
        building_bounds[0][2] = 65.61772920710077;
        building_bounds[0][3] = 22.138151464469043;
        //SW Corner
        building_bounds[0][4] = 65.61633136857452;
        building_bounds[0][5] = 22.136729625661815;
        //SE Corner
        building_bounds[0][6] = 65.61672997863523;
        building_bounds[0][7] = 22.1391037658097;

        // B
    }
}
