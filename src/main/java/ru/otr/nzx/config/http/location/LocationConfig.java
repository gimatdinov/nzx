package ru.otr.nzx.config.http.location;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class LocationConfig extends Config {
	public static enum LocationType {
		EMPTY, FILE, PROXY_PASS
	};

	public final static String INDEX = "index";
	public final static String PATH = "path";
	public final static String ENABLE = "enable";
	public final static String POST_PROCESSING_ENABLE = "post_processing_enable";
	public final static String DUMP_CONTENT_STORE = "dump_content_store";

	public final static String PROXY_PASS = "proxy_pass";
	public final static String PROXY_SET_HEADERS = "proxy_set_headers";

	public final static String FILE = "file";
	public final static String MIME_TYPE = "mime_type";

	public LocationType type;
	public int index;
	public final String path;
	public boolean enable;
	public boolean post_processing_enable;
	public final String dump_content_store;

	public URI proxy_pass;
	public final Map<String, String> proxy_set_headers = new HashMap<String, String>();

	public final String file;
	public final String mimeType;

	public LocationConfig(int index, String path, String route, Map<String, Object> routes) throws URISyntaxException {
		super(route + "/" + index, routes);
		this.index = index;
		this.path = path;
		this.dump_content_store = null;
		this.file = null;
		this.mimeType = null;
	}

	public LocationConfig(int index, String path, JSONObject src, String route, final Map<String, Object> routes) throws URISyntaxException {
		super(route + "/" + index, routes);
		this.index = index;
		this.path = path;
		enable = src.optBoolean(ENABLE, true);
		post_processing_enable = src.optBoolean(POST_PROCESSING_ENABLE, false);
		dump_content_store = src.optString(DUMP_CONTENT_STORE, null);

		if (src.has(PROXY_PASS)) {
			type = LocationType.PROXY_PASS;
			proxy_pass = new URI(src.getString(PROXY_PASS));
			JSONArray headerArray = src.optJSONArray(PROXY_SET_HEADERS);
			if (headerArray != null) {
				for (int i = 0; i < headerArray.length(); i++) {
					JSONObject header = headerArray.getJSONObject(i);
					proxy_set_headers.put(header.getString("name"), header.getString("value"));
				}
			}
		}

		if (src.has(FILE)) {
			if (type != null) {
				throw new IllegalArgumentException("Incompatible {" + PROXY_PASS + ", " + FILE + "}");
			}
			type = LocationType.FILE;
			file = src.optString(FILE);
			mimeType = src.getString(MIME_TYPE);
		} else {
			file = null;
			mimeType = null;
		}

		if (type == null) {
			type = LocationType.EMPTY;
		}
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put(INDEX, index);
		json.put(PATH, path);
		json.put(ENABLE, enable);

		json.put(POST_PROCESSING_ENABLE, post_processing_enable);
		json.put(DUMP_CONTENT_STORE, dump_content_store);

		if (proxy_pass != null) {
			json.put(PROXY_PASS, proxy_pass);
			for (Map.Entry<String, String> item : proxy_set_headers.entrySet()) {
				json.append(PROXY_SET_HEADERS, new JSONObject().put("name", item.getKey()).put("value", item.getValue()));
			}
		}

		if (file != null) {
			json.put(FILE, file);
			json.put(MIME_TYPE, mimeType);
		}

		return json;
	}

	public static String cleanPath(String path) {
		path = path.trim().replaceAll("/+", "/");
		if (path.length() > 1 && path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

}
