package ru.otr.nzx.config;

import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONObject;

import ru.otr.nzx.config.ftp.FTPConfig;
import ru.otr.nzx.config.http.HTTPConfig;

public class NZXConfig extends Config {
    public final static String NAME = "name";
    public final static String LOG_CONFIG = "log_config";
    public final static String LOG = "log";
    public final static String CONFIG_SERVICE_PORT = "config_service_port";
    public final static String FTP = "ftp";
    public final static String HTTP = "http";

    private String name;
    public final String log_config;
    public final String log;
    public final int config_service_port;
    public final FTPConfig ftp;
    public final HTTPConfig http;

    public NZXConfig(JSONObject src, Map<String, Object> routes) throws URISyntaxException {
        super("/", routes);
        name = src.optString(NAME, null);
        log_config = src.optString(LOG_CONFIG, null);
        log = src.optString(LOG, "log");
        config_service_port = src.optInt(CONFIG_SERVICE_PORT, 0);
        if (src.has(FTP)) {
            ftp = new FTPConfig(src.getJSONObject(FTP), "/" + FTP, routes);
        } else {
            ftp = null;
        }
        if (src.has(HTTP)) {
            http = new HTTPConfig(src.getJSONObject(HTTP), "/" + HTTP, routes);
        } else {
            http = null;
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject config = new JSONObject();
        if (name != null) {
            config.put(NAME, name);
        }
        if (log_config != null) {
            config.put(LOG_CONFIG, log_config);
        }
        if (!"log".equals(log)) {
            config.put(LOG, log);
        }
        if (config_service_port > 0) {
            config.put(CONFIG_SERVICE_PORT, config_service_port);
        }
        if (ftp != null) {
            config.put(FTP, ftp.toJSON());
        }
        if (http != null) {
            config.put(HTTP, http.toJSON());
        }
        return config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
