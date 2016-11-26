package ru.otr.nzx.postprocessing;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cxc.jex.postprocessing.Action;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.http.location.LocationConfig;

public class Dumping implements Action<Tank> {
    private static final DateFormat idDateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss_SSS");
    private static final DateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void process(Tank tank, Tracer tracer) {
        if (tank.contentLength > 0) {
            String dump_content_store = tank.properties.get(LocationConfig.DUMP_CONTENT_STORE);
            if (dump_content_store != null) {
                StringBuilder path = new StringBuilder();
                path.append(dump_content_store);
                path.append(File.separator);
                path.append(dayDateFormat.format(tank.requestDateTime));
                File dir = new File(path.toString());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                path.append(File.separator);
                path.append(idDateFormat.format(tank.requestDateTime));
                path.append("_");
                path.append(tank.requestID);
                path.append("_");
                path.append(tank.requestURI.getPath().replaceAll("[^ \\w]", "_"));
                path.append("_");
                path.append(tank.type);

                tracer.debug("Dumping", "file=[" + path + "] size=" + tank.contentLength);

                try (FileOutputStream fos = new FileOutputStream(path.toString())) {
                    fos.write(tank.data, 0, tank.contentLength);
                } catch (Exception e) {
                    tracer.error("Dumping.Error/NOTIFY_ADMIN", "file=[" + path + "]", e);
                }
            }
        }
    }

}
