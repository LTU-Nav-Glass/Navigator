package se.ltu.navigator.fingerprint;

import android.content.Context;
import android.net.wifi.WifiManager;

import se.ltu.navigator.MainActivity;

public class FingerprintManager {
    private final MainActivity mainActivity;
    private final WifiManager wifiManager;

    public FingerprintManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.wifiManager = (WifiManager) mainActivity.getSystemService(Context.WIFI_SERVICE);
    }


}
