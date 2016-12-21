package ru.otr.nzx.extra.dumping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.model.ActionConfig;
import ru.otr.nzx.http.postprocessing.NZXAction;
import ru.otr.nzx.http.postprocessing.NZXTank;
import ru.otr.nzx.util.NZXUtil;

public class Dumping extends NZXAction {
    public static final String DUMPS_STORE = "dumps_store";

    private static final DateFormat idDateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss_SSS");
    private static final DateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private String dumps_store;

    public Dumping(ActionConfig config) {
        super(config);
    }

    @Override
    public synchronized void applyParameters() throws Exception {
        File store = new File(config.parameters.get(DUMPS_STORE)).getAbsoluteFile();
        if (!store.exists() && !store.mkdirs()) {
            throw new Exception("Cannot make directory [" + store.getPath() + "]");
        }
        this.dumps_store = store.toString();
        config.parameters.updatedMark = false;
    }

    @Override
    public void process(NZXTank tank, Tracer tracer) throws Exception {
        if (config.parameters.updatedMark) {
            try {
                applyParameters();
            } catch (Exception e) {
                tracer.error("Dumping." + config.getName() + ".UpdateParameters.Error/NOTIFY_ADMIN", NZXUtil.tankToShortLine(tank), e);
            }
        }
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
        return ("POST".equals(tank.httpMethod) && tank.getBuffer().getContentLength() > 0);
    }

    public static String makePath(NZXTank tank) {
        StringBuilder path = new StringBuilder();
        path.append(tank.locationName);
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
