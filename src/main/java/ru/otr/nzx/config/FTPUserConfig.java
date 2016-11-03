package ru.otr.nzx.config;

import org.json.JSONObject;

public class FTPUserConfig {
	public final String name;
	public final String password;
	public final String folder;

	public FTPUserConfig(JSONObject src) {
		name = src.getString("name");
		password = src.getString("password");
		folder = src.getString("folder");
	}

	public JSONObject toJSON() {
		JSONObject user = new JSONObject();
		user.put("name", name);
		user.put("password", password);
		user.put("folder", folder);
		return user;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

}
