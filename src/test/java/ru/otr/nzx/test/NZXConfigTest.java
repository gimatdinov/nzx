package ru.otr.nzx.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.otr.nzx.config.NZXConfig;

public class NZXConfigTest {
    private final static Logger log = LoggerFactory.getLogger(NZXConfigTest.class);

    public static void main(String... args) throws URISyntaxException, IOException {
        NZXConfig config = new NZXConfig(new File("src/test/config/nzx-test.conf"));
        log.debug(""+config.http.servers.get(0).locate("/mitm/y/i"));
    }

}
