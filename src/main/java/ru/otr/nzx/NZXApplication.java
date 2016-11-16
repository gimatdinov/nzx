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
import ru.otr.nzx.config.NZXConfig;

@SpringBootApplication
public class NZXApplication implements CommandLineRunner {
    private static Logger log = LoggerFactory.getLogger(NZXApplication.class);

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
        Option nameOption = Option.builder("n").longOpt("name").required(true).numberOfArgs(1).desc("Server name").build();
        Option configOption = Option.builder("c").longOpt("config").required(true).numberOfArgs(1).desc("Path to configuration file").build();
        options.addOption(nameOption);
        options.addOption(configOption);
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdLine = parser.parse(options, args);
            String serverName = cmdLine.getOptionValue("name");
            File configFile = new File(cmdLine.getOptionValue("config")).getAbsoluteFile();
            Tracer tracer = new LogbackTracer(serverName);
            tracer.debug("Config.File", configFile.getPath());
            nzx = new NZX(new NZXConfig(configFile), tracer);
            nzx.bootstrap();
            nzx.start();

        } catch (ParseException e) {
            formatter.printHelp("java [-Dlogging.config=logback.xml] -jar nzx.jar", options);
        } catch (Exception e) {
            log.error("Application has stopped!", e);
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(NZXApplication.class, args);
    }
}
