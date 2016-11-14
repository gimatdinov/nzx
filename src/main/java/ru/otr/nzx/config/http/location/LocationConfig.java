package ru.otr.nzx.config.http.location;

import org.json.JSONObject;

public abstract class LocationConfig {
    public final static String PATH = "path";

    public final String path;

    public LocationConfig(String path) {
        this.path = path;
    }

    public JSONObject toJSON() {
        return new JSONObject().put(PATH, path);
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

}
