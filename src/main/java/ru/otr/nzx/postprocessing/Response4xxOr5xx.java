package ru.otr.nzx.postprocessing;

import cxc.jex.postprocessing.Action;
import cxc.jex.postprocessing.Tank;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.HTTPServer.ObjectType;

public class Response4xxOr5xx implements Action {

    private final String marker;
    private final int maxContentLength;

    public Response4xxOr5xx(String marker, String maxContentLength) {
        this.marker = marker;
        this.maxContentLength = Integer.valueOf(maxContentLength);
    }

    @Override
    public void process(Tank t, Tracer tracer) {
        NZXTank tank = (NZXTank) t;
        if (tank.type == ObjectType.RES && tank.getContentLength() <= maxContentLength && tank.responseStatusCode >= 400 && tank.responseStatusCode < 600) {
            tracer.info("Response.StatusCode." + tank.responseStatusCode + "/" + marker, tank.toString());
        }
    }

}
