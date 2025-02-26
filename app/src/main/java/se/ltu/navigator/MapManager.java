package se.ltu.navigator;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.util.ArrayList;

import se.ltu.navigator.location.UserLocationManager;

public class MapManager {
    private static final String TAG = "MapManager";
    private final String LTU_MAP_FILENAME = "planet_22.13,65.615_22.151,65.621.map";
    //private final int PDF_WIDTH = 2480;
    //private final int PDF_HEIGHT = 3508;
    private CompassManager compassManager;
    private UserLocationManager userLocationManager;
    private MainActivity mainActivity;
    private AssetManager assetManager;
    private MapView mapView;
    private PdfRenderer pdfRenderer;
    private Bitmap pdfBitmap;
    private ImageView pdfImageView;

    private ArrayList<ArrayList<String>> asset_filenames; // Holds array of building map filenames
    private String currentFilename;
    public MapManager(MainActivity mainActivity, CompassManager compassManager) {
        this.mainActivity = mainActivity;
        this.compassManager = compassManager;

        assetManager = mainActivity.getAssets();
        userLocationManager = compassManager.getUserLocationManager();
        asset_filenames = new ArrayList<>();
        initMapList();

        currentFilename = LTU_MAP_FILENAME; //set to LTU map first
    }

    public void mapSetupHandler()
    {
        switch(currentFilename)
        {
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
    public void switchCurrentFloor(int floor)
    {
        if(floor < 0 || floor > 3)
            return;

        String building = currentFilename.substring(0,1);

        if(building.compareTo("A") == 0)
        {
            currentFilename = asset_filenames.get(0).get(floor-1);
        } else if (building.compareTo("B") == 0) // No maps available for other buildings but is here for transparency for implementing in the future
        {

        }
    }

    public MapView getMapView(){return mapView;}
    public Bitmap getPDFBitmap(){return pdfBitmap;}
    public String getCurrentFilename(){return currentFilename;}
    public boolean useLTUMap(){return currentFilename.compareTo(LTU_MAP_FILENAME) == 0;}

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

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void mapPDFSetup()
    {
        try {
            // display pdfImageView to mapView
            pdfImageView = mainActivity.findViewById(R.id.pdfImageView);
            File tempFile = File.createTempFile("temp_pdf", ".pdf", mainActivity.getCacheDir());

            if(tempFile.exists())
            {
                InputStream inputStream = assetManager.open(currentFilename);
                FileOutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[1024];
                int size = inputStream.read(buffer);
                while(size != -1)
                {
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

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initMapList()
    {
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
     * This method measures if the user is currently in a building
     * @return true or false based off if the user is in a building or not
     */
    private boolean inBuilding()
    {
        // Can manually compare user coordinates with coordinates of buildings to see if they are in range

        return true;
    }

}
