package ru.otr.nzx.config.http.location;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.SimpleConfig;

public class LocationConfig extends Config {
    public static enum LocationType {
        EMPTY, FILE, PROXY_PASS, PROCESSOR
    };

    public final static String PATH = "path";
    public final static String ENABLE = "enable";
    public final static String POST_PROCESSING_ENABLE = "post_processing_enable";

    public final static String PROXY_PASS = "proxy_pass";
    public final static String PROXY_SET_HEADERS = "proxy_set_headers";

    public final static String FILE = "file";
    public final static String MIME_TYPE = "mime_type";

    public final static String PROCESSOR_NAME = "processor_name";

    public LocationType type;
    public final String path;
    public boolean enable;
    public boolean post_processing_enable;

    public URI proxy_pass;
    public final SimpleConfig proxy_set_headers;

    public final String file;
    public final String mimeType;

    public final String processor_name;

    public LocationConfig(String name, String path, LocationConfigMap locations) throws URISyntaxException {
        super(name, locations);
        this.path = new URI(path).normalize().getPath();
        locations.put(path, this);
        proxy_set_headers = new SimpleConfig(PROXY_SET_HEADERS, this);
        file = null;
        mimeType = null;
        processor_name = null;
    }

    LocationConfig(JSONObject src, LocationConfigMap locations) throws URISyntaxException {
        super(src.getString(NAME), locations);
        path = new URI(src.getString(PATH)).normalize().getPath();
        enable = src.optBoolean(ENABLE, true);
        post_processing_enable = src.optBoolean(POST_PROCESSING_ENABLE, false);

        if (src.has(PROXY_PASS)) {
            type = LocationType.PROXY_PASS;
            proxy_pass = new URI(src.getString(PROXY_PASS));
        }
        proxy_set_headers = new SimpleConfig(src.optJSONObject(PROXY_SET_HEADERS), PROXY_SET_HEADERS, this);

        if (src.has(FILE)) {
            if (type != null) {
                throw new IllegalArgumentException("Incompatible {" + PROXY_PASS + ", " + FILE + "}");
            }
            type = LocationType.FILE;
            file = src.optString(FILE);
            mimeType = src.getString(MIME_TYPE);
        } else {
            file = null;
            mimeType = null;
        }

        if (src.has(PROCESSOR_NAME)) {
            if (type != null) {
                throw new IllegalArgumentException("Incompatible {" + PROXY_PASS + ", " + FILE + ", " + PROCESSOR_NAME + "}");
            }
            type = LocationType.PROCESSOR;
            processor_name = src.getString(PROCESSOR_NAME);
        } else {
            processor_name = null;
        }

        if (type == null) {
            type = LocationType.EMPTY;
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(NAME, name);
        json.put(PATH, path);
        json.put(ENABLE, enable);
        json.put(POST_PROCESSING_ENABLE, post_processing_enable);

        if (type == LocationType.PROXY_PASS) {
            json.put(PROXY_PASS, proxy_pass);
            json.put(PROXY_SET_HEADERS, proxy_set_headers.toJSON());
        }

        if (type == LocationType.FILE) {
            json.put(FILE, file);
            json.put(MIME_TYPE, mimeType);
        }

        if (type == LocationType.PROCESSOR) {
            json.put(PROCESSOR_NAME, processor_name);
        }

        return json;
    }

    public static String cleanPath(String path) {
        path = path.trim().replaceAll("/+", "/");
        if (path.length() > 1 && path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public void delete() {
        unbindPathName();
        ((LocationConfigMap) host).remove(path);
    }

}
