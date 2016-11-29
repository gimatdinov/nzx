package ru.otr.nzx.postprocessing;

import cxc.jex.postprocessing.Action;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.HTTPServer.ObjectType;
import ru.otr.nzx.util.NZXUtil;

public class FailHttpResponseProcessing implements Action<NZXTank> {

    private final String marker;
    private final boolean httpSC400;
    private final boolean httpSC500;
    private final boolean httpSuccess;

    public FailHttpResponseProcessing(String marker, String httpSC400, String httpSC500, String httpSuccess) {
        this.marker = marker;
        this.httpSC400 = new Boolean(httpSC400);
        this.httpSC500 = new Boolean(httpSC500);
        this.httpSuccess = new Boolean(httpSuccess);
    }

    @Override
    public void process(NZXTank tank, Tracer tracer) {
        if (tank.type == ObjectType.RES) {
            boolean flag = false;
            if (httpSC400 && tank.responseStatusCode >= 400 && tank.responseStatusCode <= 499) {
                flag = true;
            }
            if (httpSC500 && tank.responseStatusCode >= 500 && tank.responseStatusCode <= 599) {
                flag = true;
            }
            if (httpSuccess && !tank.success) {
                flag = true;
            }
            if (flag) {
                tracer.info("Response.StatusCode." + tank.responseStatusCode + "/" + marker, NZXUtil.tankToShortLine(tank));
            }
        }
    }

}
