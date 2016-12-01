package ru.otr.nzx.config.http.location;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class ProxyPassLocationConfig extends LocationConfig {
    public final static String PROXY_PASS = "proxy_pass";
    public final static String PROXY_SET_HEADERS = "proxy_set_headers";

    private URI proxy_pass;
    public final Map<String, String> proxy_set_headers;

    public ProxyPassLocationConfig(String path, JSONObject src, String route, final Map<String, Config> routes) throws URISyntaxException {
        super(path, src, route, routes);
        proxy_pass = new URI(src.getString(PROXY_PASS));

        proxy_set_headers = new HashMap<String, String>();
        JSONArray headerArray = src.optJSONArray(PROXY_SET_HEADERS);
        if (headerArray != null) {
            for (int i = 0; i < headerArray.length(); i++) {
                JSONObject header = headerArray.getJSONObject(i);
                proxy_set_headers.put(header.getString("name"), header.getString("value"));
            }
        }

    }

    @Override
    public JSONObject toJSON() {
        JSONObject location = super.toJSON();
        location.put(PROXY_PASS, proxy_pass);
        for (Map.Entry<String, String> item : proxy_set_headers.entrySet()) {
            location.append(PROXY_SET_HEADERS, new JSONObject().put("name", item.getKey()).put("value", item.getValue()));
        }
        return location;
    }

    public URI getProxyPass() {
        return proxy_pass;
    }

    public void setProxyPass(String uri) throws URISyntaxException {
        this.proxy_pass = new URI(uri);
    }

}
