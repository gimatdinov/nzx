package ru.otr.nzx.config.http.location;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;

import ru.otr.nzx.config.Config;

public class LocationConfigMap extends Config implements Map<String, LocationConfig> {
    private Map<String, LocationConfig> map = new ConcurrentHashMap<>();

    private int counter = 0;

    public LocationConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
        super(name, host);
        if (src != null) {
            for (int i = 0; i < src.length(); i++) {
                LocationConfig item = new LocationConfig(src.getJSONObject(i), this);
                map.put(item.path, item);
            }
        }
    }

    @Override
    public Object toJSON() {
        JSONArray json = new JSONArray();
        for (Config item : map.values()) {
            json.put(item.toJSON());
        }
        return json;
    }

    public synchronized int getCounter() {
        return counter++;
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
    public LocationConfig get(Object key) {
        return map.get(key);
    }

    @Override
    public LocationConfig put(String key, LocationConfig value) {
        return map.put(key, value);
    }

    @Override
    public LocationConfig remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends LocationConfig> m) {
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
    public Collection<LocationConfig> values() {
        return map.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, LocationConfig>> entrySet() {
        return map.entrySet();
    }

}
