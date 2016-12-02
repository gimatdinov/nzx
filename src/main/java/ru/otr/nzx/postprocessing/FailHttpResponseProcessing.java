package ru.otr.nzx.postprocessing;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.HTTPServer.ObjectType;
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

	@Override
	public void loadParameters() {
		this.marker = getConfig().getParameters().get(MARKER);
		this.sc_400 = new Boolean(getConfig().getParameters().get(SC_400));
		this.sc_500 = new Boolean(getConfig().getParameters().get(SC_500));
		this.not_success = new Boolean(getConfig().getParameters().get(NOT_SUCCESS));
	}

	@Override
	public void process(NZXTank tank, Tracer tracer) {
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
				tracer.info("Response.StatusCode." + tank.responseStatusCode + "/" + marker, NZXUtil.tankToShortLine(tank));
			}
		}
	}

}
