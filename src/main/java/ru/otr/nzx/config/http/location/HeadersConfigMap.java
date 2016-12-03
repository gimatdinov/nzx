package ru.otr.nzx.config.http.location;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.otr.nzx.config.Config;

public class HeadersConfigMap extends Config implements Map<String, String> {
    public final static String VALUE = "value";
    private Map<String, String> map = new ConcurrentHashMap<>();

    public HeadersConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
        super(name, host);
        if (src != null) {
            for (int i = 0; i < src.length(); i++) {
                JSONObject header = src.getJSONObject(i);
                map.put(header.getString(NAME), header.getString(VALUE));
            }
        }
    }

    @Override
    public Object toJSON() {
        JSONArray json = new JSONArray();
        for (Map.Entry<String, String> item : map.entrySet()) {
            JSONObject header = new JSONObject();
            header.put(NAME, item.getKey());
            header.put(VALUE, item.getValue());
            json.put(header);
        }
        return json;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return map.get(key);
    }

    @Override
    public String put(String key, String value) {
        return map.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<String> values() {
        return map.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        return map.entrySet();
    }

}
