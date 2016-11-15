package ru.otr.nzx.config.http.location;

import org.json.JSONObject;

public abstract class LocationConfig {
    public final static String PATH = "path";
    public final static String POST_PROCESSING_ENABLE = "post_processing_enable";
    public final static String DUMP_CONTENT_STORE = "dump_content_store";

    public final String path;
    public final boolean post_processing_enable;
    public final String dump_content_store;

    public LocationConfig(String path, JSONObject src) {
        this.path = path;
        post_processing_enable = src.optBoolean(POST_PROCESSING_ENABLE, false);
        dump_content_store = src.optString(DUMP_CONTENT_STORE, null);

    }

    public JSONObject toJSON() {
        JSONObject location = new JSONObject();
        location.put(PATH, path);
        location.put(POST_PROCESSING_ENABLE, post_processing_enable);
        location.put(DUMP_CONTENT_STORE, dump_content_store);
        return location;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

}