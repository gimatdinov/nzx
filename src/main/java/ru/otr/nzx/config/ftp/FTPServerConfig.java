package ru.otr.nzx.config.ftp;

import java.net.URISyntaxException;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class FTPServerConfig extends Config {
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

    public final FTPUserConfigMap users;

    public String getListen() {
        return listenHost + ":" + listenPort;
    }

    public FTPServerConfig(JSONObject src, FTPServerConfigMap servers) throws URISyntaxException {
        super(src.getString(NAME), servers);
        enable = src.optBoolean(ENABLE, true);
        name = src.getString(NAME);
        String[] listen = src.getString(LISTEN).split(":");
        listenHost = listen[0];
        listenPort = Integer.valueOf(listen[1]);

        active_enable = src.optBoolean(ACTIVE_ENABLE, false);
        passive_ports = src.optString(PASSIVE_PORTS, null);

        directory = src.getString(DIRECTORY);
        anonymous_enable = src.optBoolean(ANONYMOUS_ENABLE);

        users = new FTPUserConfigMap(src.optJSONArray(USERS), USERS, this);

    }

    @Override
    public Object toJSON() {
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
        server.put(USERS, users.toJSON());
        return server;
    }

}
