package ru.otr.nzx.postprocessing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cxc.jex.postprocessing.Action;
import cxc.jex.tracer.Tracer;

public class Dumping implements Action<NZXTank> {
    private static final DateFormat idDateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss_SSS");
    private static final DateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final String dumps_store;

    public Dumping(String dumps_store) {
        this.dumps_store = dumps_store;
    }

    @Override
    public void process(NZXTank tank, Tracer tracer) throws Exception {
        if (isProcess(tank)) {
            String fullPath = dumps_store + "/" + makePath(tank);
            tracer.debug("Dumping", makePath(tank) + " size=" + tank.getBuffer().getContentLength());
            File dir = new File(fullPath).getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(fullPath.toString())) {
                tank.getBuffer().read(fos);
            } catch (IllegalAccessException | IOException e) {
                tracer.error("Dumping.Error/NOTIFY_ADMIN", "file=[" + fullPath + "] size=" + tank.getBuffer().getContentLength(), e);
            }
        }
    }

    public static boolean isProcess(NZXTank tank) {
        return (tank.dumping_enable && tank.getBuffer().getContentLength() > 0);
    }

    public static String makePath(NZXTank tank) {
        StringBuilder path = new StringBuilder();
        path.append(tank.location_name);
        path.append("/");
        path.append(dayDateFormat.format(tank.requestDateTime));
        path.append("/");
        path.append(idDateFormat.format(tank.requestDateTime));
        path.append("_");
        path.append(tank.requestID);
        path.append("_");
        path.append(tank.requestURI.getPath().replaceAll("[^ \\w]", "_"));
        path.append("_");
        path.append(tank.type);
        return path.toString();
    }

    @Override
    public boolean isEnable() {
        return true;
    }

}
