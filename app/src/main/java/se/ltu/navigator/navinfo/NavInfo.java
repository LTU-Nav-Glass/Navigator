package se.ltu.navigator.navinfo;

import java.util.Observer;

import se.ltu.navigator.R;
import se.ltu.navigator.util.ObservableString;

public enum NavInfo {
    CURRENT_LOCATION(R.string.title_current_location),
    TARGET_LOCATION(R.string.title_target_location),
    COMPASS_ACCURACY(R.string.title_compass_accuracy),
    AZIMUTH(R.string.title_azimuth),
    BEARING(R.string.title_bearing),
    DISTANCE(R.string.title_distance);

    private final int title;
    private final ObservableString data;

    NavInfo(int title) {
        this.title = title;
        this.data = new ObservableString("-");
    }

    public int getTitle() {
        return title;
    }

    public String getData() {
        return data.getValue();
    }

    public void setData(String data) {
        this.data.setValue(data);
    }

    public void registerListener(Observer o) {
        this.data.addObserver(o);
    }

    public void unregisterListener(Observer o) {
        this.data.deleteObserver(o);
    }
}
