package ru.otr.nzx.config.model;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.json.JSONObject;

public class NZXConfig extends Config {
    public final static String SERVER_NAME = "server_name";
    public final static String LOG_CONFIG = "log_config";
    public final static String LOG = "log";
    public final static String CONFIG_SERVICE_PORT = "config_service_port";
    public final static String FTP = "ftp";
    public final static String HTTP = "http";

    private String server_name;
    public final String log_config;
    public final String log;
    public final int config_service_port;
    public final HTTPConfig http;

    public NZXConfig(JSONObject src) throws URISyntaxException, UnknownHostException {
        super(null, null);
        server_name = src.optString(SERVER_NAME, InetAddress.getLocalHost().getHostName());
        log_config = src.optString(LOG_CONFIG, null);
        log = src.optString(LOG, "log");
        config_service_port = src.optInt(CONFIG_SERVICE_PORT, 0);
        http = new HTTPConfig(src.getJSONObject(HTTP), HTTP, this);
    }

    @Override
    public Object toJSON() {
        JSONObject json = new JSONObject();
        if (server_name != null) {
            json.put(SERVER_NAME, name);
        }
        if (log_config != null) {
            json.put(LOG_CONFIG, log_config);
        }
        if (!"log".equals(log)) {
            json.put(LOG, log);
        }
        if (config_service_port > 0) {
            json.put(CONFIG_SERVICE_PORT, config_service_port);
        }
        json.put(HTTP, http.toJSON());
        return json;
    }

    public String getServerName() {
        return server_name;
    }

    public void setServerName(String server_name) {
        this.server_name = server_name;
    }

}
