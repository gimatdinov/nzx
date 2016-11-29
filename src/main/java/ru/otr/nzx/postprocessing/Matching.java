package ru.otr.nzx.postprocessing;

import java.io.ByteArrayOutputStream;

import cxc.jex.postprocessing.Action;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.HTTPServer.ObjectType;
import ru.otr.nzx.util.NZXUtil;

public class Matching implements Action<NZXTank> {

    private final String marker;
    private final int maxContentLength;
    private final String regex;

    public Matching(String marker, String maxContentLength, String regex) {
        this.marker = marker;
        this.maxContentLength = Integer.valueOf(maxContentLength);
        this.regex = regex;
    }

    @Override
    public void process(NZXTank tank, Tracer tracer) {
        if (tank.type == ObjectType.RES && tank.getBuffer().getContentLength() > 0 && tank.getBuffer().getContentLength() <= maxContentLength) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                tank.getBuffer().read(baos);
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
