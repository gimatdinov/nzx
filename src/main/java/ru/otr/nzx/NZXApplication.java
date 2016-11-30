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

    public final static String OPTION_NAME = "name";
    public final static String OPTION_CONFIG = "config";
    public final static String DEFAULT_CONFIG_PATHNAME = "config" + File.separator + "nzx.conf";
    public final static String PROPERTY_NZX_LOG = "nzx_log";

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
            File configFile = new File(DEFAULT_CONFIG_PATHNAME).getAbsoluteFile();
            CommandLine cmdLine = parser.parse(options, args);
            if (cmdLine.getOptionValue(OPTION_CONFIG) != null) {
                configFile = new File(cmdLine.getOptionValue(OPTION_CONFIG)).getAbsoluteFile();
            }
            if (!configFile.exists()) {
                log.error("Config file not found: " + configFile.getPath());
                return;
            }
            File configDir = configFile.getParentFile();
            log.info("Config.File: " + configFile);
            NZXConfig config = new NZXConfig(configFile);
            String serverName = (config.getName() != null) ? config.getName() : InetAddress.getLocalHost().getHostName();
            if (cmdLine.getOptionValue(OPTION_NAME) != null) {
                serverName = cmdLine.getOptionValue(OPTION_NAME);
            }
            config.setName(serverName);
            if (config.log_config != null) {
                loadLogConfig(configDir.getPath() + File.separator + config.log_config, config.log);
            }
            Tracer tracer = new LogbackTracer("NZX");
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
            configurator.getContext().putProperty(PROPERTY_NZX_LOG, logDir);
            configurator.doConfigure(logConfigFile);
        } catch (JoranException je) {
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }
    }

    public static void main(String... args) throws Exception {
        SpringApplication.run(NZXApplication.class, args);
    }
}
