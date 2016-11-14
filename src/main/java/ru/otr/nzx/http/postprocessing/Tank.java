package ru.otr.nzx.http.postprocessing;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Tank {
    public static enum Type {
        REQ, RES
    };

    private static final DateFormat idDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public Type type;
    public String requestID;
    public Date requestDateTime;
    public URI uri;
    public final byte[] data;
    public int contentLength;

    public final Map<String, String> properties = new HashMap<>();

    Tank(int capacity) {
        data = new byte[capacity];
    }

    public boolean isContentComplete() {
        return (data.length >= contentLength);
    }

    @Override
    public String toString() {
        return idDateFormat.format(requestDateTime) + " " + requestID + " " + uri.getPath() + " " + type + " LEN=" + contentLength;
    }

}