package ru.otr.nzx.config.ftp;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class FTPServerConfig extends Config {
    public final static String ENABLE = "enable";
    public final static String NAME = "name";
    public final static String LISTEN = "listen";
    public final static String ACTIVE_ENABLE = "active_enable";
    public final static String PASSIVE_PORTS = "passive_ports";
    public final static String DIRECTORY = "directory";
    public final static String ANONYMOUS_ENABLE = "anonymous_enable";
    public final static String USERS = "users";

    public final boolean enable;
    public final String name;
    public final String listenHost;
    public final int listenPort;

    public final boolean active_enable;
    public final String passive_ports;

    public final String directory;
    public final boolean anonymous_enable;

    public final List<FTPUserConfig> users;

    public String getListen() {
        return listenHost + ":" + listenPort;
    }

    public FTPServerConfig(JSONObject src, String route, Map<String, Config> routes) throws URISyntaxException {
        super(src, route + "/" + src.getString(NAME), routes);
        enable = src.optBoolean(ENABLE, true);
        name = src.getString(NAME);
        String[] listen = src.getString(LISTEN).split(":");
        listenHost = listen[0];
        listenPort = Integer.valueOf(listen[1]);

        active_enable = src.optBoolean(ACTIVE_ENABLE, false);
        passive_ports = src.optString(PASSIVE_PORTS, null);

        directory = src.getString(DIRECTORY);
        anonymous_enable = src.optBoolean(ANONYMOUS_ENABLE);

        users = new ArrayList<>();
        if (!anonymous_enable) {
            JSONArray userArray = src.getJSONArray(USERS);
            for (int i = 0; i < userArray.length(); i++) {
                users.add(new FTPUserConfig(userArray.getJSONObject(i), route + "/" + name, routes));
            }
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject server = new JSONObject();
        if (!enable) {
            server.put(ENABLE, enable);
        }
        server.put(NAME, name);
        server.put(LISTEN, getListen());
        server.put(ACTIVE_ENABLE, active_enable);
        if (passive_ports != null) {
            server.put(PASSIVE_PORTS, passive_ports);
        }
        server.put(DIRECTORY, directory);
        server.put(ANONYMOUS_ENABLE, anonymous_enable);
        for (FTPUserConfig item : users) {
            server.append(USERS, item.toJSON());
        }
        return server;
    }

}
