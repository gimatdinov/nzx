package ru.otr.nzx;

import java.io.File;

import javax.annotation.PreDestroy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import cxc.jex.tracer.Tracer;
import cxc.jex.tracer.logback.LogbackTracer;
import ru.otr.nzx.config.service.ConfigService;

@SpringBootApplication
public class NZXApplication implements CommandLineRunner {
    private static Logger log = LoggerFactory.getLogger(NZXApplication.class);

    public final static String OPTION_SERVER_NAME = "name";
    public final static String OPTION_CONFIG = "config";

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    private ConfigService cfgService;
    private NZX nzx;

    @PreDestroy
    private void fina() {
        if (nzx != null) {
            nzx.stop();
        }
        if (cfgService != null) {
            cfgService.stop();
        }
    }

    @Override
    public void run(String... args) {
        Tracer tracer = new LogbackTracer("NZX");
        File configFile = new File(ConfigService.DEFAULT_CONFIG_PATHNAME).getAbsoluteFile();
        String serverName = null;
        Options options = new Options();
        Option nameOption = Option.builder("n").longOpt(OPTION_SERVER_NAME).required(false).numberOfArgs(1).desc("Server name").build();
        Option configOption = Option.builder("c").longOpt(OPTION_CONFIG).required(false).numberOfArgs(1).desc("Path to configuration file").build();
        options.addOption(nameOption);
        options.addOption(configOption);
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmdLine = parser.parse(options, args);
            if (cmdLine.getOptionValue(OPTION_CONFIG) != null) {
                configFile = new File(cmdLine.getOptionValue(OPTION_CONFIG)).getAbsoluteFile();
            }
            if (cmdLine.getOptionValue(OPTION_SERVER_NAME) != null) {
                serverName = cmdLine.getOptionValue(OPTION_SERVER_NAME);
            }
        } catch (ParseException e) {
            formatter.printHelp("java -jar nzx.jar", options);
            applicationContext.close();
            return;
        }
        try {
            tracer.info("Loading", "NZX version: " + NZXConstants.NZX_VERSION);
            cfgService = new ConfigService(configFile, tracer);
            cfgService.bootstrap();
            if (serverName != null) {
                cfgService.nzx().setServerName(serverName);
            }
            cfgService.start();
            nzx = new NZX(cfgService, tracer);
            nzx.bootstrap();
            nzx.start();
        } catch (Exception e) {
            log.error("Application stopping...", e);
            applicationContext.close();
        }
    }

    public static void main(String... args) throws Exception {
        SpringApplication.run(NZXApplication.class, args);
    }
}