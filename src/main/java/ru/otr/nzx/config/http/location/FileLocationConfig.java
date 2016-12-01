package ru.otr.nzx.config.http.location;

import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class FileLocationConfig extends LocationConfig {
    public final static String FILE = "file";
    public final static String MIME_TYPE = "mime_type";

    public final String file;
    public final String mimeType;

    public FileLocationConfig(String path, JSONObject src, String route, final Map<String, Config> routes) throws URISyntaxException {
        super(path, src, route, routes);
        file = src.getString(FILE);
        mimeType = src.getString(MIME_TYPE);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject location = super.toJSON();
        location.put(FILE, file);
        location.put(MIME_TYPE, mimeType);
        return location;
    }

}
