package ru.otr.nzx.config.http.location;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class LocationConfig extends Config {
    public static enum LocationType {
        EMPTY, FILE, PROXY_PASS
    };

    public final static String PATH = "path";
    public final static String ENABLE = "enable";
    public final static String POST_PROCESSING_ENABLE = "post_processing_enable";
    public final static String DUMP_CONTENT_STORE = "dump_content_store";

    public final static String PROXY_PASS = "proxy_pass";
    public final static String PROXY_SET_HEADERS = "proxy_set_headers";

    public final static String FILE = "file";
    public final static String MIME_TYPE = "mime_type";

    public LocationType type;
    public int index;
    public final String path;
    public boolean enable;
    public boolean post_processing_enable;
    public final String dump_content_store;

    public URI proxy_pass;
    public final HeadersConfigMap proxy_set_headers;

    public final String file;
    public final String mimeType;

    public LocationConfig(String path, LocationConfigMap locations) throws URISyntaxException {
        super(String.valueOf(locations.getCounter()), locations);
        this.path = new URI(path).normalize().getPath();
        locations.put(path, this);
        proxy_set_headers = new HeadersConfigMap(null, PROXY_SET_HEADERS, this);
        dump_content_store = null;
        file = null;
        mimeType = null;
    }

    LocationConfig(JSONObject src, String name, LocationConfigMap locations) throws URISyntaxException {
        super(name, locations);
        path = new URI(src.getString(PATH)).normalize().getPath();
        enable = src.optBoolean(ENABLE, true);
        post_processing_enable = src.optBoolean(POST_PROCESSING_ENABLE, false);
        dump_content_store = src.optString(DUMP_CONTENT_STORE, null);

        if (src.has(PROXY_PASS)) {
            type = LocationType.PROXY_PASS;
            proxy_pass = new URI(src.getString(PROXY_PASS));
        }
        proxy_set_headers = new HeadersConfigMap(src.optJSONArray(PROXY_SET_HEADERS), PROXY_SET_HEADERS, this);

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
        json.put(DUMP_CONTENT_STORE, dump_content_store);

        if (proxy_pass != null) {
            json.put(PROXY_PASS, proxy_pass);
            json.put(PROXY_SET_HEADERS, proxy_set_headers.toJSON());
        }

        if (file != null) {
            json.put(FILE, file);
            json.put(MIME_TYPE, mimeType);
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
        getContext().remove(getPathName());
        getContext().remove(getPathName() + "/");
        ((LocationConfigMap) host).remove(path);
    }

}
