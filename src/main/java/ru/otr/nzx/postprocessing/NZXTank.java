package ru.otr.nzx.postprocessing;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cxc.jex.postprocessing.Tank;
import ru.otr.nzx.http.HTTPServer.ObjectType;

public class NZXTank implements Tank {
    private static final DateFormat idDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public ObjectType type;
    public Date requestDateTime;
    public String requestID;
    public URI requestURI;
    public int responseStatusCode;
    public boolean success;
    final byte[] data;
    public int contentLength;

    public final Map<String, String> properties = new HashMap<>();

    NZXTank(int capacity) {
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
        result.append(requestURI.getPath());
        result.append(" ");
        result.append(type);
        if (type == ObjectType.RES) {
            result.append("(");
            result.append(responseStatusCode);
            result.append(")");
        }
        result.append(" ");
        result.append("LEN=");
        result.append(contentLength);
        result.append(" ");
        result.append(success ? "success" : "unfinished");
        return result.toString();
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }
}
