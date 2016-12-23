package ru.otr.nzx.extra.dumping;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import ru.otr.nzx.config.model.ProcessorConfig;

public class DumpSearchProcessorConfig {
    public static final String DUMPS_STORE = "dumps_store";
    public static final String SKIP_IN_DUMPS_STORE = "skip_in_dumps_store";
    public static final String DUMPS_FTP_URI = "dumps_ftp_uri";

    public static final String FTP_HOST = "ftp_host";
    public static final String FTP_PORT = "ftp_port";
    public static final String FTP_DIRECTORY = "ftp_directory";

    static final String CONST_CONTENT = "content";
    static final String CONST_PATH = "path";
    static final String CONST_SEARCH_INDEX = ".search-index";

    public final File dumps_store;
    public final Set<String> skip_in_dumps_store = new HashSet<>();
    public final File search_index;
    public final String dumps_ftp_uri;

    public final String ftp_host;
    public final int ftp_port;
    public final String ftp_directory;

    public DumpSearchProcessorConfig(ProcessorConfig src) {
        dumps_store = new File(src.processor_parameters.get(DUMPS_STORE));
        skip_in_dumps_store.add(CONST_SEARCH_INDEX);
        if (src.processor_parameters.get(SKIP_IN_DUMPS_STORE) != null) {
            for (String item : src.processor_parameters.get(SKIP_IN_DUMPS_STORE).split("\\s+")) {
                if (item.length() > 0) {
                    skip_in_dumps_store.add(item);
                }
            }
        }
        search_index = new File(src.processor_parameters.get(DUMPS_STORE) + "/" + CONST_SEARCH_INDEX + "/" + src.getName()).getAbsoluteFile();
        ftp_host = src.processor_parameters.get(FTP_HOST);
        ftp_port = Integer.valueOf(src.processor_parameters.get(FTP_PORT));
        ftp_directory = src.processor_parameters.get(FTP_DIRECTORY);
        dumps_ftp_uri = src.processor_parameters.get(DUMPS_FTP_URI);
    }

}
