package ru.otr.nzx.config;

import java.util.Map;

import ru.otr.nzx.config.location.LocationConfig;

public class NZXConfigHelper {

	public static String cleanPath(String path) {
		path = path.trim().replaceAll("/+", "/");
		if (path.length() > 1 && path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	public static LocationConfig locate(String path, Map<String, LocationConfig> locations) {
		LocationConfig result = null;
		String part[] = cleanPath(path).split("/");
		for (int i = 0; i < part.length; i++) {
			StringBuilder used = new StringBuilder();
			for (int j = 0; j < part.length - i; j++) {
				used.append("/");
				used.append(part[j]);
			}
			result = locations.get(cleanPath(used.toString()));
			//System.out.println(cleanPath(used.toString()));
			if (result != null) {
				return result;
			}
		}
		return result;
	}
}
