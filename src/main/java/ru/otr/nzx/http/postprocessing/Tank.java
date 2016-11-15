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
    public Date requestDateTime;
    public String requestID;
    public URI requestURI;
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
        StringBuilder result = new StringBuilder();
        result.append(idDateFormat.format(requestDateTime));
        result.append(" ");
        result.append(requestID);
        result.append(" ");
        result.append(requestURI);
        result.append(" ");
        result.append(type);
        result.append(" ");
        result.append("LEN=");
        result.append(contentLength);
        return result.toString();
    }

}