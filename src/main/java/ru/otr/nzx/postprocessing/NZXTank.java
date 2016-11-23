package ru.otr.nzx.postprocessing;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cxc.jex.postprocessing.Tank;
import ru.otr.nzx.http.HTTPServer.ObjectType;

public class NZXTank implements Tank {
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
    public byte[] getData() {
        return data;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

}
