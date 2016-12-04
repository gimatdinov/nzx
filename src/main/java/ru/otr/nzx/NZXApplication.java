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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cxc.jex.tracer.Tracer;
import cxc.jex.tracer.logback.LogbackTracer;
import ru.otr.nzx.config.service.NZXConfigService;

@SpringBootApplication
public class NZXApplication implements CommandLineRunner {
    private static Logger log = LoggerFactory.getLogger(NZXApplication.class);

    public final static String OPTION_NAME = "name";
    public final static String OPTION_CONFIG = "config";

    private NZX nzx;

    @PreDestroy
    private void fina() {
        if (nzx != null) {
            nzx.stop();
        }
    }

    @Override
    public void run(String... args) {
        Options options = new Options();
        Option nameOption = Option.builder("n").longOpt(OPTION_NAME).required(false).numberOfArgs(1).desc("Server name").build();
        Option configOption = Option.builder("c").longOpt(OPTION_CONFIG).required(false).numberOfArgs(1).desc("Path to configuration file").build();
        options.addOption(nameOption);
        options.addOption(configOption);
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        try {
            File configFile = new File(NZXConfigService.DEFAULT_CONFIG_PATHNAME).getAbsoluteFile();
            CommandLine cmdLine = parser.parse(options, args);
            if (cmdLine.getOptionValue(OPTION_CONFIG) != null) {
                configFile = new File(cmdLine.getOptionValue(OPTION_CONFIG)).getAbsoluteFile();
            }
            Tracer tracer = new LogbackTracer("NZX");
            NZXConfigService cfgService = new NZXConfigService(configFile, tracer);
            if (cmdLine.getOptionValue(OPTION_NAME) != null) {
                cfgService.nzx().setName(cmdLine.getOptionValue(OPTION_NAME));
            }            
            nzx = new NZX(cfgService, tracer);
            nzx.bootstrap();
            nzx.start();
        } catch (ParseException e) {
            formatter.printHelp("java -jar nzx.jar", options);
        } catch (Exception e) {
            log.error("Application has stopped!", e);
        }
    }

    public static void main(String... args) throws Exception {
        SpringApplication.run(NZXApplication.class, args);
    }
}
