package ru.otr.nzx.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.json.JSONObject;

import ru.otr.nzx.config.ftp.FTPConfig;
import ru.otr.nzx.config.http.HTTPConfig;

public class NZXConfig {
    public final static String FTP = "ftp";
    public final static String HTTP = "http";

    public final FTPConfig ftp;
    public final HTTPConfig http;

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
        if (config.has(FTP)) {
            ftp = new FTPConfig(config.getJSONObject(FTP));
        } else {
            ftp = null;
        }
        http = new HTTPConfig(config.getJSONObject(HTTP));
    }

    public NZXConfig(File file) throws URISyntaxException, IOException {
        this(new String(Files.readAllBytes(file.toPath())));
    }

    public JSONObject toJSON() {
        JSONObject config = new JSONObject();
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
