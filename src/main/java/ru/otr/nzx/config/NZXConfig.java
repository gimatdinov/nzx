package ru.otr.nzx.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.json.JSONObject;

import ru.otr.nzx.config.ftp.FTPConfig;
import ru.otr.nzx.config.http.HTTPConfig;

public class NZXConfig {
    public final static String NAME = "name";
    public final static String LOG_CONFIG = "log_config";
    public final static String LOG = "log";
    public final static String FTP = "ftp";
    public final static String HTTP = "http";

    public final String name;
    public final String log_config;
    public final String log;
    public final FTPConfig ftp;
    public final HTTPConfig http;

    public NZXConfig(File file) throws URISyntaxException, IOException {
        this(new String(Files.readAllBytes(file.toPath())));
    }

    public NZXConfig(String src) throws URISyntaxException {
        String[] lines = src.split("\n");
        StringBuilder cleanSrc = new StringBuilder();
        for (String line : lines) {
            if (!line.trim().startsWith("//")) {
                cleanSrc.append(line);
                cleanSrc.append("\n");
            }
        }
        JSONObject config = new JSONObject(cleanSrc.toString());
        name = config.optString(NAME, null);
        log_config = config.optString(LOG_CONFIG, null);
        log = config.optString(LOG, "log");
        if (config.has(FTP)) {
            ftp = new FTPConfig(config.getJSONObject(FTP));
        } else {
            ftp = null;
        }
        http = new HTTPConfig(config.getJSONObject(HTTP));
    }

    public JSONObject toJSON() {
        JSONObject config = new JSONObject();
        if (name != null) {
            config.put(NAME, name);
        }
        if (log_config != null) {
            config.put(LOG_CONFIG, log_config);
        }
        if (!"log".equals(log)) {
            config.put(LOG, log);
        }
        if (ftp != null) {
            config.put(FTP, ftp.toJSON());
        }
        config.put(HTTP, http.toJSON());
        return config;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

}
