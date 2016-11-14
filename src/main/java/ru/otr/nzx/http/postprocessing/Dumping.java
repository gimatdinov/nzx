package ru.otr.nzx.http.postprocessing;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.http.location.ProxyPassLocationConfig;

public class Dumping implements HTTPPostProcessor.Action {
	private static final DateFormat idDateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss_SSS");
    private static final DateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void process(Tank tank, Tracer tracer) {
		StringBuilder path = new StringBuilder();
        path.append(tank.properties.get(ProxyPassLocationConfig.DUMP_CONTENT_STORE));
        path.append("/");
        path.append(dayDateFormat.format(tank.requestDateTime));
		File dir = new File(path.toString());
		if (!dir.exists()) {
			dir.mkdirs();
		}
        path.append("/");
        path.append(idDateFormat.format(tank.requestDateTime));
        path.append("_");
        path.append(tank.requestID);
        path.append("_");
        path.append(tank.uri.getPath().replaceAll("[^ \\w]", "_"));
        path.append("_");
        path.append(tank.type);
        
		tracer.debug("Dumping", "file=[" + path + "] size=" + tank.contentLength);
		
		try (FileOutputStream fos = new FileOutputStream(path.toString())) {
			fos.write(tank.data, 0, tank.contentLength);
		} catch (Exception e) {
			tracer.error("Dumping.Error/DUMPING_ERROR", "file=[" + path + "]", e);
		}
	}

}
