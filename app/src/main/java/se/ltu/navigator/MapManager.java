package se.ltu.navigator;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import se.ltu.navigator.location.UserLocationHandler;

public class MapManager {
    private static final String TAG = "MapManager";
    private final String LTU_MAP_FILENAME = "planet_22.13,65.615_22.151,65.621.map";
    private CompassManager compassManager;
    private MainActivity mainActivity;
    private AssetManager assetManager;
    protected MapView mapView;
    private PdfRenderer pdfRenderer;
    private Bitmap pdfBitmap;
    protected ImageView pdfImageView;
    private ArrayList<ArrayList<String>> asset_filenames; // Holds array of building map filenames
    private double[][] building_bounds;
    private int current_building_index;
    private String currentFilename;
    private UserLocationHandler userLocationHandler;

    public MapManager(MainActivity mainActivity, CompassManager compassManager) {
        this.mainActivity = mainActivity;
        this.compassManager = compassManager;

        assetManager = mainActivity.getAssets();
        userLocationHandler = compassManager.getUserLocationHandler();
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
        current_building_index = 0;
        String lastFilename = currentFilename;

        if (current_building_index == -1)
            currentFilename = LTU_MAP_FILENAME;
        else {
            // Check that user's current floor works
            currentFilename = asset_filenames.get(current_building_index).get(1);
//            Log.d(TAG, currentFilename);
        }
        if (lastFilename.compareTo(currentFilename) != 0)
            mapSetupHandler();
    }

    // ISSUE, DOES NOT SWITCH BACK TO COMPASS
    public void mapSetupHandler() {
        switch (currentFilename) {
            case LTU_MAP_FILENAME:
                mapLTUSetup();
                break;
            default:
                mapPDFSetup();
                break;
        }
    }

    /**
     * This method switches the current map displayed on app when user changes floors
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

    public boolean useLTUMap() {
        return currentFilename.compareTo(LTU_MAP_FILENAME) == 0;
    }

    private void mapLTUSetup() {
        try {
            mapView = mainActivity.findViewById(R.id.mapView);

            mapView.setClickable(false);
            mapView.getMapScaleBar().setVisible(false);
            mapView.setBuiltInZoomControls(false);

            InputStream inputStream = assetManager.open(currentFilename);

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
            mapView.setCenter(new LatLong(65.618, 22.141));
            mapView.setZoomLevel((byte) 18);

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
    public void hideLTUMap() {
        for (Layer layer : mapView.getLayerManager().getLayers()) {
            if (layer instanceof Marker || layer instanceof Polyline) { continue; }
            layer.setVisible(false);
        }
    }
    public void hidePdfMap() {
        pdfImageView.setVisibility(ImageView.INVISIBLE);
    }

    private void mapPDFSetup() {
        try {
            // display pdfImageView to mapView
            pdfImageView = mainActivity.findViewById(R.id.pdfImageView);
            File tempFile = File.createTempFile("temp_pdf", ".pdf", mainActivity.getCacheDir());

            if (tempFile.exists()) {
                InputStream inputStream = assetManager.open(currentFilename);
                FileOutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[1024];
                int size = inputStream.read(buffer);
                while (size != -1) {
                    outputStream.write(buffer, 0, size);
                    size = inputStream.read(buffer);
                }

                inputStream.close();
                outputStream.close();
            }

            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);

            // render page to bitmap
            PdfRenderer.Page pdfPage = pdfRenderer.openPage(0);
            pdfBitmap = Bitmap.createBitmap(pdfPage.getWidth(), pdfPage.getHeight(), Bitmap.Config.ARGB_8888);
            pdfPage.render(pdfBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            //Display bitmap to ImageView
            pdfImageView.setImageBitmap(pdfBitmap);

            pdfImageView.setScaleX(3);
            pdfImageView.setScaleY(3);

            pdfImageView.setRotation(-21);

//            hidePdfMap();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initMapList() {
        // A Hus
        ArrayList<String> a_Hus = new ArrayList();
        a_Hus.add("A-huset1.pdf");
        a_Hus.add("A-huset2.pdf");
        a_Hus.add("A-huset3.pdf");

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

    /**
     * This method measures if the user is within the set bounds of each building
     *
     * @return integer identifying which building user is in, -1 if user is not in a building
     */
    private int getUserBuilding()
    {

        double userLat = userLocationHandler.getLatitude();
        double userLong = userLocationHandler.getLongitude();

        for(int hus_index = 0; hus_index < building_bounds.length; hus_index++)
        {
            // Measure if user is within bounds of longitude
            if(userLat <= building_bounds[hus_index][0]
                    && userLat <= building_bounds[hus_index][2]
                    && userLat >= building_bounds[hus_index][4]
                    && userLat >= building_bounds[hus_index][6])
            {
                //Log.d(TAG, "UserLat True");
                // Measure if user is within bounds of latitude
                if(userLong > building_bounds[hus_index][1]
                        && userLong > building_bounds[hus_index][5]
                        && userLong < building_bounds[hus_index][3]
                        && userLong < building_bounds[hus_index][7])
                {
                    //Log.d(TAG, "User in A");
                    return hus_index;
                }
            }

        }

        return -1; //returns if user is not in a building
    }
}
