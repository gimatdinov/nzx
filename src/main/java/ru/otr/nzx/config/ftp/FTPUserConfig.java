package ru.otr.nzx.config.ftp;

import org.json.JSONObject;

public class FTPUserConfig {
	public final static String NAME = "name";
	public final static String PASSWORD = "password";
	public final static String FOLDER = "folder";

	public final String name;
	public final String password;
	public final String folder;

	public FTPUserConfig(JSONObject src) {
		name = src.getString(NAME);
		password = src.getString(PASSWORD);
		folder = src.getString(FOLDER);
	}

	public JSONObject toJSON() {
		JSONObject user = new JSONObject();
		user.put(NAME, name);
		user.put(PASSWORD, password);
		user.put(FOLDER, folder);
		return user;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

}
