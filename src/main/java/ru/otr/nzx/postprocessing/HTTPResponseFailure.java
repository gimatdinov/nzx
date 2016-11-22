package ru.otr.nzx.postprocessing;

import cxc.jex.postprocessing.Action;
import cxc.jex.postprocessing.Tank;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.HTTPServer.ObjectType;

public class HTTPResponseFailure implements Action {

	private final String marker;
	private final boolean httpSC400;
	private final boolean httpSC500;
	private final boolean httpSuccess;

	public HTTPResponseFailure(String marker, String httpSC400, String httpSC500, String httpSuccess) {
		this.marker = marker;
		this.httpSC400 = new Boolean(httpSC400);
		this.httpSC500 = new Boolean(httpSC500);
		this.httpSuccess = new Boolean(httpSuccess);
	}

	@Override
	public void process(Tank t, Tracer tracer) {
		NZXTank tank = (NZXTank) t;
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
				tracer.info("Response.StatusCode." + tank.responseStatusCode + "/" + marker, tank.toString());
			}
		}
	}

}
