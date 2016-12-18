package ru.otr.nzx.config;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

public class SimpleConfig extends Config implements Map<String, String> {

    public final Map<String, String> map = new ConcurrentHashMap<>();

    public SimpleConfig(String name, Config host) throws URISyntaxException {
        super(name, host);
    }

    public SimpleConfig(JSONObject src, String name, Config host) throws URISyntaxException {
        super(name, host);
        if (src != null) {
            for (Object key : src.keySet()) {
                map.put((String) key, src.getString((String) key));
            }
        }
    }

    @Override
    public Object toJSON() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, String> item : map.entrySet()) {
            if (item.getValue() != null && item.getValue().length() > 0) {
                json.put(item.getKey(), item.getValue());
            }
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
