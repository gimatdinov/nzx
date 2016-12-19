package ru.otr.nzx.extra.dumpsearch;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import ru.otr.nzx.config.SimpleConfig;

public class DumpSearchProcessorConfig {
	public static final String DUMPS_STORE = "dumps_store";
	public static final String SKIP_IN_DUMPS_STORE = "skip_in_dumps_store";
	public static final String REINDEX_ON_START = "reindex_on_start";
	public static final String FTP_SERVER = "ftp_server";

	static final String CONST_CONTENT = "content";
	static final String CONST_PATH = "path";
	static final String CONST_SEARCH_INDEX = ".search-index";

	public final File dumps_store;
	public final Set<String> skip_in_dumps_store = new HashSet<>();
	public final boolean reindex_on_start;
	public final File search_index;
	public final String ftp_server;

	public DumpSearchProcessorConfig(String serverName, SimpleConfig src) {
		dumps_store = new File(src.get(DUMPS_STORE));
		skip_in_dumps_store.add(CONST_SEARCH_INDEX);
		if (src.get(SKIP_IN_DUMPS_STORE) != null) {
			for (String item : src.get(SKIP_IN_DUMPS_STORE).split("\\s+")) {
				if (item.length() > 0) {
					skip_in_dumps_store.add(item);
				}
			}
		}
		reindex_on_start = Boolean.valueOf(src.get(REINDEX_ON_START));
		search_index = new File(src.get(DUMPS_STORE) + "/" + CONST_SEARCH_INDEX + "/" + serverName).getAbsoluteFile();
		ftp_server = src.get(FTP_SERVER);
	}

}
