package ru.otr.nzx.http.postprocessing;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Tank {
	public static enum Type {
		REQ, RES
	};
	
	public Type type;
	public String requestID;
	public Date requestDateTime;
	public URI uri;
	public final byte[] data;
	public int contentLength;

	public final Map<String, String> properties = new HashMap<>();

	Tank(int capacity) {
		data = new byte[capacity];
	}

}