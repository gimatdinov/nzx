package ru.otr.nzx.config.ftp;

import java.net.URISyntaxException;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class FTPConfig extends Config {
    public final static String SERVERS = "servers";

    public final FTPServerConfigMap servers;

    public FTPConfig(JSONObject src, String name, Config host) throws URISyntaxException {
        super(name, host);
        servers = new FTPServerConfigMap(src.getJSONArray(SERVERS), SERVERS, this);
    };

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(SERVERS, servers.toJSON());
        return json;
    }

}
