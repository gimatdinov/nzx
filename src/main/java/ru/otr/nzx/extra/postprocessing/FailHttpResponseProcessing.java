package ru.otr.nzx.extra.postprocessing;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.postprocessing.HTTPMessageAction;
import ru.otr.nzx.http.postprocessing.HTTPMessageTank;
import ru.otr.nzx.http.server.HTTPServer.ObjectType;
import ru.otr.nzx.util.NZXUtil;

public class FailHttpResponseProcessing extends HTTPMessageAction {

    public static final String MARKER = "marker";
    public static final String SC_400 = "SC_400";
    public static final String SC_500 = "SC_500";
    public static final String NOT_SUCCESS = "not_success";

    private String marker;
    private boolean sc_400;
    private boolean sc_500;
    private boolean not_success;

    @Override
    public synchronized void applyParameters() throws Exception {
        this.marker = getConfig().parameters.get(MARKER);
        this.sc_400 = new Boolean(getConfig().parameters.get(SC_400));
        this.sc_500 = new Boolean(getConfig().parameters.get(SC_500));
        this.not_success = new Boolean(getConfig().parameters.get(NOT_SUCCESS));
        getConfig().parametersUpdatedMark = false;
    }

    @Override
    public void process(HTTPMessageTank tank, Tracer tracer) {
        if (getConfig().parametersUpdatedMark) {
            try {
                applyParameters();
            } catch (Exception e) {
                tracer.error("FailHttpResponseProcessing." + config.getName() + ".UpdateParameters.Error/NOTIFY_ADMIN", NZXUtil.tankToShortLine(tank), e);
            }
        }
        if (tank.type == ObjectType.RES) {
            boolean flag = false;
            if (sc_400 && tank.responseStatusCode >= 400 && tank.responseStatusCode <= 499) {
                flag = true;
            }
            if (sc_500 && tank.responseStatusCode >= 500 && tank.responseStatusCode <= 599) {
                flag = true;
            }
            if (not_success && !tank.success) {
                flag = true;
            }
            if (flag) {
                tracer.info("FailHttpResponseProcessing." + config.getName() + "/" + marker, NZXUtil.tankToShortLine(tank));
            }
        }
    }

}
