package ru.otr.nzx.extra.postprocessing;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.model.ActionConfig;
import ru.otr.nzx.http.postprocessing.NZXAction;
import ru.otr.nzx.http.postprocessing.NZXTank;
import ru.otr.nzx.http.server.Server.ObjectType;
import ru.otr.nzx.util.NZXUtil;

public class FailHttpResponseProcessing extends NZXAction {
    public static final String MARKER = "marker";
    public static final String SC_400 = "SC_400";
    public static final String SC_500 = "SC_500";
    public static final String NOT_SUCCESS = "not_success";

    private String marker;
    private boolean sc_400;
    private boolean sc_500;
    private boolean not_success;

    public FailHttpResponseProcessing(ActionConfig config) {
        super(config);
    }

    @Override
    public synchronized void applyParameters() throws Exception {
        marker = config.parameters.get(MARKER);
        sc_400 = new Boolean(config.parameters.get(SC_400));
        sc_500 = new Boolean(config.parameters.get(SC_500));
        not_success = new Boolean(config.parameters.get(NOT_SUCCESS));
        config.parameters.updatedMark = false;
    }

    @Override
    public void process(NZXTank tank, Tracer tracer) {
        if (config.parameters.updatedMark) {
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
