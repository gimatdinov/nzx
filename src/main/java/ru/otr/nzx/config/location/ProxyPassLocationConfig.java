package ru.otr.nzx.config.location;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProxyPassLocationConfig extends LocationConfig {

	public final URI proxy_pass;
	public final Map<String, String> proxy_set_headers;

	public final boolean dump_body_POST;
	public final String dump_body_store;

	public ProxyPassLocationConfig(String path, JSONObject src) throws URISyntaxException {
		super(path);
		proxy_pass = new URI(src.getString("proxy_pass"));

		proxy_set_headers = new HashMap<String, String>();
		JSONArray headerArray = src.optJSONArray("proxy_set_headers");
		if (headerArray != null) {
			for (Object item : headerArray) {
				JSONObject header = (JSONObject) item;
				proxy_set_headers.put(header.getString("name"), header.getString("value"));
			}
		}
		dump_body_POST = src.optBoolean("dump_body_POST");
		if (dump_body_POST) {
			dump_body_store = src.getString("dump_body_store");
		} else {
			dump_body_store = null;
		}
	}

	@Override
	public JSONObject toJSON() {
		JSONObject location = super.toJSON();
		location.put("proxy_pass", proxy_pass);
		for (Map.Entry<String, String> item : proxy_set_headers.entrySet()) {
			location.append("proxy_set_headers", new JSONObject().put("name", item.getKey()).put("value", item.getValue()));
		}
		location.put("dump_body_POST", dump_body_POST);
		location.put("dump_body_store", dump_body_store);
		return location;
	}

}
