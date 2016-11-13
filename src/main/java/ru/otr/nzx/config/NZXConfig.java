package ru.otr.nzx.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.json.JSONObject;

public class NZXConfig {
    public final static String FTP = "ftp";
    public final static String HTTP = "http";
    public final static String DUMPER = "dumper";

    public final FTPConfig ftp;
    public final HTTPConfig http;
    public final DumperConfig dumper;

    public NZXConfig(String src) throws URISyntaxException {
        JSONObject config = new JSONObject(src);
        if (config.has(FTP)) {
            ftp = new FTPConfig(config.getJSONObject(FTP));
        } else {
            ftp = null;
        }
        http = new HTTPConfig(config.getJSONObject(HTTP));
        if (config.has(DUMPER)) {
            dumper = new DumperConfig(config.getJSONObject(DUMPER));
        } else {
            dumper = null;
        }
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
        if (dumper != null) {
            config.put(DUMPER, dumper.toJSON());
        }
        return config;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

}
