package ru.otr.nzx;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cxc.jex.tracer.Tracer;
import cxc.jex.tracer.logback.LogbackTracer;
import ru.otr.nzx.config.NZXConfig;

@SpringBootApplication
public class NZXApplication implements CommandLineRunner {

    @Override
    public void run(String... args) {
        Tracer tracer = new LogbackTracer("NZX");
        try {
            if (args.length != 2) {
                throw new IllegalArgumentException("Usage: java -jar nzx.jar <name> <path to nzx.conf>");
            }
            String name = args[0];
            File configFile = new File(args[1]).getAbsoluteFile();
            String config = new String(Files.readAllBytes(configFile.toPath()));
            tracer.debug("Application.Run.Config", "[" + configFile + "]=" + config);
            NZX nzx = new NZX(name, new NZXConfig(config), tracer);
            nzx.bootstrap();
            tracer.info("Application.Run.Start/NOTIFY_ADMIN","");
            nzx.start();
        } catch (IOException | URISyntaxException e) {
            tracer.error("Application.Run.Error", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(NZXApplication.class, args);
    }
}
