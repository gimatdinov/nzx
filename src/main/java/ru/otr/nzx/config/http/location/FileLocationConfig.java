package ru.otr.nzx.config.http.location;

import org.json.JSONObject;

public class FileLocationConfig extends LocationConfig {
    public final static String FILE = "file";
    public final static String MIME_TYPE = "mime_type";

    public final String file;
    public final String mimeType;

    public FileLocationConfig(String path, JSONObject src) {
        super(path, src);
        file = src.getString(FILE);
        mimeType = src.getString(MIME_TYPE);
    }

    public JSONObject toJSON() {
        JSONObject location = super.toJSON();
        location.put(FILE, file);
        location.put(MIME_TYPE, mimeType);
        return location;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }
}
