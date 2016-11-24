package ru.otr.nzx;

import java.io.File;
import java.net.InetAddress;

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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
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
        Option configOption = Option.builder("c").longOpt("config").required(false).numberOfArgs(1).desc("Path to configuration file").build();
        options.addOption(configOption);
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        try {
            File configFile = new File("config" + File.separator + "nzx.conf").getAbsoluteFile();
            CommandLine cmdLine = parser.parse(options, args);
            if (cmdLine.getOptionValue("config") != null) {
                configFile = new File(cmdLine.getOptionValue("config")).getAbsoluteFile();
            }
            if (!configFile.exists()) {
                log.error("Config file not found: " + configFile.getPath());
                return;
            }
            File configDir = configFile.getParentFile();
            log.info("Config.File: " + configFile);
            NZXConfig config = new NZXConfig(configFile);
            String serverName = (config.name != null) ? config.name : InetAddress.getLocalHost().getHostName();
            if (config.log_config != null) {
                loadLogConfig(configDir.getPath() + File.separator + config.log_config, config.log);
            }
            Tracer tracer = new LogbackTracer(serverName);
            nzx = new NZX(config, tracer);
            nzx.bootstrap();
            nzx.start();
        } catch (ParseException e) {
            formatter.printHelp("java -jar nzx.jar", options);
        } catch (Exception e) {
            log.error("Application has stopped!", e);
        }
    }

    private void loadLogConfig(String logConfigFile, String logDir) {
        log.info("Log.Config.File: " + logConfigFile);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.getContext().putProperty("nzx_log", logDir);
            configurator.doConfigure(logConfigFile);
        } catch (JoranException je) {
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }
    }

    public static void main(String... args) throws Exception {
        SpringApplication.run(NZXApplication.class, args);
    }
}
