package ru.otr.nzx.config.http;

import java.net.URISyntaxException;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.http.processing.HTTPProcessorConfigMap;
import ru.otr.nzx.config.http.server.HTTPServerConfigMap;

public class HTTPConfig extends Config {
    public final static String PROCESSORS = "processors";
    public final static String SERVERS = "servers";

    public final HTTPProcessorConfigMap processors;
    public final HTTPServerConfigMap servers;

    public HTTPConfig(JSONObject src, String name, Config host) throws URISyntaxException {
        super(name, host);
        if (src.has(PROCESSORS)) {
            processors = new HTTPProcessorConfigMap(src.getJSONArray(PROCESSORS), PROCESSORS, this);
        } else {
            processors = null;
        }
        servers = new HTTPServerConfigMap(src.getJSONArray(SERVERS), SERVERS, this);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject http = new JSONObject();
        if (processors != null) {
            http.put(PROCESSORS, processors.toJSON());
        }
        http.put(SERVERS, servers.toJSON());
        return http;
    }
}
