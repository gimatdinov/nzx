package ru.otr.nzx.config.location;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProxyPassLocationConfig extends LocationConfig {
    public final static String PROXY_PASS = "proxy_pass";
    public final static String PROXY_SET_HEADERS = "proxy_set_headers";
    public final static String DUMP_CONTENT_ENABLE = "dump_content_enable";
    public final static String DUMP_CONTENT_STORE = "dump_content_store";

    public final URI proxy_pass;
    public final Map<String, String> proxy_set_headers;

    public final boolean dump_content_enable;
    public final String dump_content_store;

    public ProxyPassLocationConfig(String path, JSONObject src) throws URISyntaxException {
        super(path);
        proxy_pass = new URI(src.getString(PROXY_PASS));

        proxy_set_headers = new HashMap<String, String>();
        JSONArray headerArray = src.optJSONArray(PROXY_SET_HEADERS);
        if (headerArray != null) {
            for (int i = 0; i < headerArray.length(); i++) {
                JSONObject header = headerArray.getJSONObject(i);
                proxy_set_headers.put(header.getString("name"), header.getString("value"));
            }
        }
        dump_content_enable = src.optBoolean(DUMP_CONTENT_ENABLE);
        if (dump_content_enable) {
            dump_content_store = src.getString(DUMP_CONTENT_STORE);
        } else {
            dump_content_store = null;
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject location = super.toJSON();
        location.put(PROXY_PASS, proxy_pass);
        for (Map.Entry<String, String> item : proxy_set_headers.entrySet()) {
            location.append(PROXY_SET_HEADERS, new JSONObject().put("name", item.getKey()).put("value", item.getValue()));
        }
        location.put(DUMP_CONTENT_ENABLE, dump_content_store);
        location.put(DUMP_CONTENT_STORE, dump_content_store);
        return location;
    }

}
