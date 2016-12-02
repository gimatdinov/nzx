package ru.otr.nzx.postprocessing;

import java.io.ByteArrayOutputStream;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.http.HTTPServer.ObjectType;
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

	@Override
	public void loadParameters() {
		this.marker = getConfig().getParameters().get(MARKER);
		this.object_type = ObjectType.valueOf(getConfig().getParameters().get(OBJECT_TYPE));
		this.сontent_length_max = Integer.valueOf(getConfig().getParameters().get(CONTENT_LENGTH_MAX));
		this.uri_regex = getConfig().getParameters().get(URI_REGEX);
		this.content_regex = getConfig().getParameters().get(CONTENT_REGEX);
	}

	@Override
	public void process(NZXTank tank, Tracer tracer) {
		if (tank.type == object_type && tank.getBuffer().getContentLength() > 0 && tank.getBuffer().getContentLength() <= сontent_length_max) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				tank.getBuffer().read(baos);
				String content = baos.toString();
				if (uri_regex.matches(tank.requestURI.toString()) && content.matches(content_regex)) {
					tracer.info("Matching." + marker + "/" + marker, NZXUtil.tankToShortLine(tank));
				}

			} catch (Exception e) {
				tracer.error("Matching." + marker + ".Error/" + marker, NZXUtil.tankToShortLine(tank), e);
			} finally {
			}
		}
	}

}
