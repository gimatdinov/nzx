package ru.otr.nzx.extra.postprocessing;

import java.io.ByteArrayOutputStream;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.model.ActionConfig;
import ru.otr.nzx.http.postprocessing.NZXAction;
import ru.otr.nzx.http.postprocessing.NZXTank;
import ru.otr.nzx.http.server.Server.ObjectType;
import ru.otr.nzx.util.NZXUtil;

public class Matching extends NZXAction {
    public static final String MARKER = "marker";
    public static final String OBJECT_TYPE = "object_type";
    public static final String CONTENT_LENGTH_MAX = "content_length_max";
    public static final String URI_REGEX = "uri_regex";
    public static final String CONTENT_REGEX = "content_regex";

    private String marker;
    private ObjectType object_type;
    private int сontent_length_max;
    private String uri_regex;
    private String content_regex;

    public Matching(ActionConfig config) {
        super(config);
    }

    @Override
    public synchronized void applyParameters() throws Exception {
        marker = config.parameters.get(MARKER);
        object_type = ObjectType.valueOf(config.parameters.get(OBJECT_TYPE));
        сontent_length_max = Integer.valueOf(config.parameters.get(CONTENT_LENGTH_MAX));
        uri_regex = config.parameters.get(URI_REGEX);
        content_regex = config.parameters.get(CONTENT_REGEX);
        config.parameters.updatedMark = false;
    }

    @Override
    public void process(NZXTank tank, Tracer tracer) throws Exception {
        if (config.parameters.updatedMark) {
            try {
                applyParameters();
            } catch (Exception e) {
                tracer.error("Matching." + config.getName() + ".UpdateParameters.Error/NOTIFY_ADMIN", NZXUtil.tankToShortLine(tank), e);
            }
        }
        if (tank.type == object_type && tank.getBuffer().getContentLength() > 0 && tank.getBuffer().getContentLength() <= сontent_length_max) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                tank.getBuffer().read(baos);
                String content = baos.toString();
                if (uri_regex.matches(tank.requestURI.toString()) && content.matches(content_regex)) {
                    tracer.info("Matching." + config.getName() + "/" + marker, NZXUtil.tankToShortLine(tank));
                }
            }
        }
    }

}
