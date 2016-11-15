package ru.otr.nzx.http.postprocessing;

import java.io.ByteArrayOutputStream;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.postprocessing.Tank.Type;

public class Parsing implements HTTPPostProcessor.Action {

    private final String marker;
    private final int maxContentLength;
    private final String regex;

    public Parsing(String marker, String maxContentLength, String regex) {
        this.marker = marker;
        this.maxContentLength = Integer.valueOf(maxContentLength);
        this.regex = regex;
    }

    @Override
    public void process(Tank tank, Tracer tracer) {
        if (tank.type == Type.RES && tank.contentLength <= maxContentLength) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                baos.write(tank.data, 0, tank.contentLength);
                String content = baos.toString();
                if (content.matches(regex)) {
                    tracer.info("Parsing." + marker + "/" + marker, tank.toString());
                }

            } catch (Exception e) {
                tracer.error("Parsing." + marker + ".Error/NOTIFY_ADMIN", tank.toString(), e);
            }
        }

    }

}
