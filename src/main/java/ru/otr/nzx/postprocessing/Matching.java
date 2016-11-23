package ru.otr.nzx.postprocessing;

import java.io.ByteArrayOutputStream;

import cxc.jex.postprocessing.Action;
import cxc.jex.postprocessing.Tank;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.HTTPServer.ObjectType;
import ru.otr.nzx.util.NZXUtil;

public class Matching implements Action {

    private final String marker;
    private final int maxContentLength;
    private final String regex;

    public Matching(String marker, String maxContentLength, String regex) {
        this.marker = marker;
        this.maxContentLength = Integer.valueOf(maxContentLength);
        this.regex = regex;
    }

    @Override
    public void process(Tank t, Tracer tracer) {
        NZXTank tank = (NZXTank) t;
        if (tank.type == ObjectType.RES && tank.getContentLength() <= maxContentLength) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                baos.write(tank.getData(), 0, tank.getContentLength());
                String content = baos.toString();
                if (content.matches(regex)) {
                    tracer.info("Matching." + marker + "/" + marker, NZXUtil.tankToShortLine(tank));
                }

            } catch (Exception e) {
                tracer.error("Matching." + marker + ".Error/NOTIFY_ADMIN", NZXUtil.tankToShortLine(tank), e);
            }
        }

    }

}
