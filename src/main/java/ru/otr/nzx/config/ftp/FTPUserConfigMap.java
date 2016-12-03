package ru.otr.nzx.config.ftp;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.ConfigMap;

public class FTPUserConfigMap extends ConfigMap<FTPUserConfig> {

    public FTPUserConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
        super(src, name, host);
    }

    @Override
    protected FTPUserConfig makeItem(JSONObject src, String name) throws URISyntaxException {
        return new FTPUserConfig(src, this);
    }

}
