package se.ltu.navigator.util;

import java.util.Observable;

public class ObservableString extends Observable {
    private String value;

    public ObservableString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.setChanged();
        this.notifyObservers(value);
    }
}
