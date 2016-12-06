package ru.otr.nzx.config;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class ConfigMap<E extends Config> extends Config implements Map<String, E> {
	private Map<String, E> map = new ConcurrentHashMap<>();

	public ConfigMap(JSONArray src, String name, Config host) throws URISyntaxException {
		super(name, host);
		if (src != null) {
			for (int i = 0; i < src.length(); i++) {
				E item = makeItem(src.getJSONObject(i));
				map.put(item.name, item);
			}
		}
	}

	protected abstract E makeItem(JSONObject src) throws URISyntaxException;

	@Override
	public Object toJSON() {
		JSONArray json = new JSONArray();
		for (Config item : map.values()) {
			json.put(item.toJSON());
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
	public E get(Object key) {
		return map.get(key);
	}

	@Override
	public E put(String key, E value) {
		return map.put(key, value);
	}

	@Override
	public E remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends E> m) {
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
	public Collection<E> values() {
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, E>> entrySet() {
		return map.entrySet();
	}

}
