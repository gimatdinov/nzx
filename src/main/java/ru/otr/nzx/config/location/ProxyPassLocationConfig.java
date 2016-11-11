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
    public final static String DUMP_BODY_POST = "dump_body_POST";
    public final static String DUMP_BODY_STORE = "dump_body_store";

    public final URI proxy_pass;
    public final Map<String, String> proxy_set_headers;

    public final boolean dump_body_POST;
    public final String dump_body_store;

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
        dump_body_POST = src.optBoolean(DUMP_BODY_POST);
        if (dump_body_POST) {
            dump_body_store = src.getString(DUMP_BODY_STORE);
        } else {
            dump_body_store = null;
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject location = super.toJSON();
        location.put(PROXY_PASS, proxy_pass);
        for (Map.Entry<String, String> item : proxy_set_headers.entrySet()) {
            location.append(PROXY_SET_HEADERS, new JSONObject().put("name", item.getKey()).put("value", item.getValue()));
        }
        location.put(DUMP_BODY_POST, dump_body_POST);
        location.put(DUMP_BODY_STORE, dump_body_store);
        return location;
    }

}
