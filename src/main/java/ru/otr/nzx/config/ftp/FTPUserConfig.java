package ru.otr.nzx.config.ftp;

import java.net.URISyntaxException;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class FTPUserConfig extends Config {
    public final static String PASSWORD = "password";
    public final static String FOLDER = "folder";

    public final String name;
    public final String password;
    public final String folder;

    public FTPUserConfig(JSONObject src, FTPUserConfigMap users) throws URISyntaxException {
        super(src.getString(NAME), users);
        name = src.getString(NAME);
        password = src.getString(PASSWORD);
        folder = src.getString(FOLDER);
    }

    @Override
    public Object toJSON() {
        JSONObject json = new JSONObject();
        json.put(NAME, name);
        json.put(PASSWORD, password);
        json.put(FOLDER, folder);
        return json;
    }

}
