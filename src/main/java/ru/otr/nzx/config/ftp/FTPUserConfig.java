package ru.otr.nzx.config.ftp;

import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class FTPUserConfig extends Config {
    public final static String NAME = "name";
    public final static String PASSWORD = "password";
    public final static String FOLDER = "folder";

    public final String name;
    public final String password;
    public final String folder;

    public FTPUserConfig(JSONObject src, String route, Map<String, Object> routes) throws URISyntaxException {
        super(route + "/" + src.getString(NAME), routes);
        name = src.getString(NAME);
        password = src.getString(PASSWORD);
        folder = src.getString(FOLDER);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject user = new JSONObject();
        user.put(NAME, name);
        user.put(PASSWORD, password);
        user.put(FOLDER, folder);
        return user;
    }

}
