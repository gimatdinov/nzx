package ru.otr.nzx.config.model;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.otr.nzx.config.service.ConfigException;

public class ServerConfig extends Config {
    private final static Logger log = LoggerFactory.getLogger(ServerConfig.class);

    public final static String LISTEN = "listen";

    public final static String CONNECT_TIMEOUT = "connect_timeout";
    public final static String IDLE_CONNECTION_TIMEOUT = "idle_connection_timeout";
    public final static String MAX_REQUEST_BUFFER_SIZE = "max_request_buffer_size";
    public final static String MAX_RESPONSE_BUFFER_SIZE = "max_response_buffer_size";

    public final static String LOCATIONS = "locations";

    public final boolean enable;
    public final String listenHost;
    public final int listenPort;

    public final int connect_timeout;
    public final int idle_connection_timeout;

    public final int max_request_buffer_size;
    public final int max_response_buffer_size;

    public final LocationConfigMap locations;

    public String getListen() {
        return listenHost + ":" + listenPort;
    }

    public ServerConfig(JSONObject src, Config host) throws ConfigException {
        super(src.getString(NAME), host);
        enable = src.optBoolean(ENABLE, true);
        String[] listen = src.getString(LISTEN).split(":");
        listenHost = listen[0];
        listenPort = Integer.valueOf(listen[1]);

        connect_timeout = src.optInt(CONNECT_TIMEOUT);
        idle_connection_timeout = src.optInt(IDLE_CONNECTION_TIMEOUT);

        max_request_buffer_size = src.optInt(MAX_REQUEST_BUFFER_SIZE);
        max_response_buffer_size = src.optInt(MAX_RESPONSE_BUFFER_SIZE);

        locations = new LocationConfigMap(src.getJSONArray(LOCATIONS), LOCATIONS, this);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject server = new JSONObject();
        if (!enable) {
            server.put(ENABLE, enable);
        }
        server.put(NAME, name);
        server.put(LISTEN, getListen());

        if (connect_timeout > 0) {
            server.put(CONNECT_TIMEOUT, connect_timeout);
        }
        if (idle_connection_timeout > 0) {
            server.put(IDLE_CONNECTION_TIMEOUT, idle_connection_timeout);
        }
        if (max_request_buffer_size > 0) {
            server.put(MAX_REQUEST_BUFFER_SIZE, max_request_buffer_size);
        }
        if (max_response_buffer_size > 0) {
            server.put(MAX_RESPONSE_BUFFER_SIZE, max_response_buffer_size);
        }
        server.put(LOCATIONS, locations.toJSON());
        return server;
    }

    public LocationConfig locate(String path) {
        path = (path != null) ? path : "/";
        String part[] = LocationConfig.cleanPath(path).split("/");
        if (part.length == 0) {
            return locations.get("/");
        }
        LocationConfig loc = null;
        for (int i = 0; i < part.length; i++) {
            StringBuilder used = new StringBuilder();
            for (int j = 0; j < part.length - i; j++) {
                used.append("/");
                used.append(part[j]);
            }
            String cursor = LocationConfig.cleanPath(used.toString());
            log.debug(path + " > " + cursor);
            loc = locations.get(cursor);
            if (loc != null) {
                log.debug(path + " = " + loc.path);
                return loc;
            }
        }
        log.debug(path + " not found");
        return null;
    }

}
