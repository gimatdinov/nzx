package ru.otr.nzx.config.model;

import org.json.JSONObject;

public class HTTPConfig extends Config {
	public final static String SERVERS = "servers";
	public final static String PROCESSORS = "processors";
	public final static String POST_PROCESSORS = "post_processors";

	public final ServerConfigMap servers;
	public final ProcessorConfigMap processors;
	public final PostProcessorConfigMap post_processors;

	public HTTPConfig(JSONObject src, String name, Config host) throws ConfigException {
		super(name, host);
		if (src.has(PROCESSORS)) {
			processors = new ProcessorConfigMap(src.getJSONArray(PROCESSORS), PROCESSORS, this);
		} else {
			processors = null;
		}
		if (src.has(POST_PROCESSORS)) {
			post_processors = new PostProcessorConfigMap(src.getJSONArray(POST_PROCESSORS), POST_PROCESSORS, this);
		} else {
			post_processors = null;
		}
		servers = new ServerConfigMap(src.getJSONArray(SERVERS), SERVERS, this);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject http = new JSONObject();
		if (processors != null) {
			http.put(PROCESSORS, processors.toJSON());
		}
		if (post_processors != null) {
			http.put(POST_PROCESSORS, post_processors.toJSON());
		}
		http.put(SERVERS, servers.toJSON());
		return http;
	}
}
