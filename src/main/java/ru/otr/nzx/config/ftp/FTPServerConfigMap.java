package ru.otr.nzx.config.ftp;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.ConfigMap;

public class FTPServerConfigMap extends ConfigMap<FTPServerConfig> {

    public FTPServerConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
        super(src, name, host);
    }

    @Override
    protected FTPServerConfig makeItem(JSONObject src, String name) throws URISyntaxException {
        return new FTPServerConfig(src, this);
    }

}
