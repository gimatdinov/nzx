package ru.otr.nzx.http.postprocessing;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import ru.otr.nzx.http.server.Server.ObjectType;

public class NZXTank extends cxc.jex.postprocessing.Tank {
    public ObjectType type;
    public Date requestDateTime;
    public String requestID;
    public URI requestURI;
    public int responseStatusCode;
    public boolean success;

    public String locationName;
    public String httpMethod;

    public final Map<String, String> properties = new HashMap<>();

    public void writeContent(ByteBuf content) {
        int rix = content.readerIndex();
        while (content.readableBytes() > 0) {
            getBuffer().writeByte(content.readByte());
        }
        content.setIndex(rix, content.writerIndex());
    }

}
