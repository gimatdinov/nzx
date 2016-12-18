package ru.otr.nzx.extra.dumpsearch;

import java.io.File;

import ru.otr.nzx.config.SimpleConfig;

public class DumpSearchProcessorConfig {
    public static final String DUMPS_STORE = "dumps_store";
    public static final String REINDEX_ON_START = "reindex_on_start";
    public static final String FTP_SERVER = "ftp_server";

    static final String CONST_SEARCH_INDEX = ".search-index";
    static final String CONST_CONTENT = "content";
    static final String CONST_PATH = "path";

    public final File dumps_store;
    public final boolean reindex_on_start;
    public final File search_index;
    public final String ftp_server;

    public DumpSearchProcessorConfig(SimpleConfig src) {
        dumps_store = new File(src.get(DUMPS_STORE));
        reindex_on_start = Boolean.valueOf(src.get(REINDEX_ON_START));
        search_index = new File(src.get(DUMPS_STORE) + "/" + CONST_SEARCH_INDEX).getAbsoluteFile();
        ftp_server = src.get(FTP_SERVER);
    }

}
