package ru.otr.nzx;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.PreDestroy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cxc.jex.tracer.Tracer;
import cxc.jex.tracer.logback.LogbackTracer;
import ru.otr.nzx.config.NZXConfig;

@SpringBootApplication
public class NZXApplication implements CommandLineRunner {

    private NZX nzx;

    @PreDestroy
    private void fina() {
        nzx.stop();
    }

    @Override
    public void run(String... args) {

        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java -jar nzx.jar <name> <path to nzx.conf>");
        }
        String name = args[0];
        File configFile = new File(args[1]).getAbsoluteFile();

        Tracer tracer = new LogbackTracer(name);
        try {
            tracer.debug("Config.File", configFile.getPath());
            nzx = new NZX(new NZXConfig(configFile), tracer);
            nzx.bootstrap();
            nzx.start();
        } catch (IOException | URISyntaxException e) {
            tracer.error("Error", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(NZXApplication.class, args);
    }
}
