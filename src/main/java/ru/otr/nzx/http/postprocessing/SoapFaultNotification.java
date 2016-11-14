package ru.otr.nzx.http.postprocessing;

import java.io.ByteArrayOutputStream;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.postprocessing.Tank.Type;

public class SoapFaultNotification implements HTTPPostProcessor.Action {

    private final int maxContentLength;
    private final String regex;

    public SoapFaultNotification(String maxContentLength, String regex) {
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
                    tracer.info("SoapFault/SOAP_FAULT", tank.toString());
                }

            } catch (Exception e) {
                tracer.error("SoapFaultNotification.Error/SOAP_FAULT", tank.toString(), e);
            }
        }

    }

}
