package ru.otr.nzx.postprocessing;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.otr.nzx.http.HTTPServer.ObjectType;

public class Tank {
    public ObjectType type;
    public Date requestDateTime;
    public String requestID;
    public URI requestURI;
    public int responseStatusCode;
    public boolean success;
    public final byte[] data;
    public int contentLength;

    public final Map<String, String> properties = new HashMap<>();

    Tank(int capacity) {
        data = new byte[capacity];
    }

    public boolean isContentComplete() {
        return (data.length >= contentLength);
    }

}
