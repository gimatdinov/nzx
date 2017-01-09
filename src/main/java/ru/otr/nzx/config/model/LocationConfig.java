package ru.otr.nzx.config.model;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationConfig extends Config {
    public static enum LocationType {
        EMPTY, FILE, PROXY_PASS, PROCESSOR, CLASS
    };

    public final static String PATH = "path";
    public final static String ENABLE = "enable";

    public final static String PROXY_PASS = "proxy_pass";
    public final static String PROXY_SET_HEADERS = "proxy_set_headers";

    public final static String FILE = "file";
    public final static String MIME_TYPE = "mime_type";

    public final static String PROCESSOR_NAME = "processor_name";

    public final static String LOCATION_CLASS = "location_class";
    public final static String LOCATION_PARAMETERS = "location_parameters";

    public final static String POST_PROCESSOR_NAME = "post_processor_name";

    public LocationType type;
    public final String path;
    public boolean enable;

    public URI proxy_pass;
    public final SimpleConfig proxy_set_headers;

    public final String file;
    public final String mimeType;

    public final String processor_name;

    public final String location_class;
    public final SimpleConfig location_parameters;

    public final String post_processor_name;

    public LocationConfig(String name, String path, LocationConfigMap locations) throws ConfigException {
        super(name, locations);
        try {
            this.path = new URI(path).normalize().getPath();
        } catch (URISyntaxException e) {
            throw new ConfigException(e.getMessage(), e);
        }
        locations.put(path, this);
        proxy_set_headers = new SimpleConfig(PROXY_SET_HEADERS, this);
        file = null;
        mimeType = null;
        processor_name = null;
        location_class = null;
        location_parameters = null;
        post_processor_name = null;
    }

    LocationConfig(JSONObject src, LocationConfigMap locations) throws ConfigException {
        super(src.getString(NAME), locations);
        try {
            path = new URI(src.getString(PATH)).normalize().getPath();
            enable = src.optBoolean(ENABLE, true);

            if (src.has(PROXY_PASS)) {
                type = LocationType.PROXY_PASS;
                proxy_pass = new URI(src.getString(PROXY_PASS));
            }
            proxy_set_headers = new SimpleConfig(src.optJSONObject(PROXY_SET_HEADERS), PROXY_SET_HEADERS, this);
        } catch (JSONException e) {
            throw new ConfigException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new ConfigException(e.getMessage(), e);
        }
        if (src.has(FILE)) {
            if (type != null) {
                throw new ConfigException("Incompatible {" + PROXY_PASS + ", " + FILE + "}");
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
                throw new ConfigException("Incompatible {" + PROXY_PASS + ", " + FILE + ", " + PROCESSOR_NAME + "}");
            }
            type = LocationType.PROCESSOR;
            processor_name = src.getString(PROCESSOR_NAME);
            Config processorConfig = context.get(REF + processor_name);
            if (processorConfig == null) {
                throw new ConfigException("Processor with " + REF + processor_name + " not found, need for locations[name=\"" + getName() + "\"]");
            }
            if (!(processorConfig instanceof ProcessorConfig)) {
                throw new ConfigException(REF + processor_name + "\"] is not reference to Processor, need for locations[name=\"" + getName() + "\"]");
            }
            if (!((ProcessorConfig) processorConfig).enable) {
                throw new ConfigException("Processor with " + REF + processor_name + " not enable, need for locations[name=\"" + getName() + "\"]");
            }
        } else {
            processor_name = null;
        }

        if (src.has(LOCATION_CLASS)) {
            if (type != null) {
                throw new ConfigException("Incompatible {" + PROXY_PASS + ", " + FILE + ", " + LOCATION_CLASS + "}");
            }
            type = LocationType.CLASS;
            location_class = src.getString(LOCATION_CLASS);
            if (enable) {
                try {
                    Class.forName(location_class);
                } catch (ClassNotFoundException e) {
                    throw new ConfigException(src.toString(), e);
                }
            }
            location_parameters = new SimpleConfig(src.optJSONObject(LOCATION_PARAMETERS), LOCATION_PARAMETERS, this);
        } else {
            location_class = null;
            location_parameters = null;
        }

        if (type == null) {
            type = LocationType.EMPTY;
        }

        post_processor_name = src.optString(POST_PROCESSOR_NAME, null);
        if (post_processor_name != null) {
            Config postProcessorConfig = context.get(REF + post_processor_name);
            if (postProcessorConfig == null) {
                throw new ConfigException("PostProcessor with " + REF + post_processor_name + " not found, need for locations[name=\"" + getName() + "\"]");
            }
            if (!(postProcessorConfig instanceof PostProcessorConfig)) {
                throw new ConfigException(REF + post_processor_name + "\"] is not reference to PostProcessor, need for locations[name=\"" + getName() + "\"]");
            }
            if (!((PostProcessorConfig) postProcessorConfig).enable) {
                throw new ConfigException("PostProcessor with " + REF + post_processor_name + " not enable, need for locations[name=\"" + getName() + "\"]");
            }
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(NAME, name);
        json.put(PATH, path);
        json.put(ENABLE, enable);

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

        if (type == LocationType.CLASS) {
            json.put(LOCATION_CLASS, location_class);
            json.put(LOCATION_PARAMETERS, location_parameters.toJSON());
        }

        json.put(POST_PROCESSOR_NAME, post_processor_name);

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
