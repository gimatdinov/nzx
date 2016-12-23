package ru.otr.nzx.config.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.model.ActionConfig;
import ru.otr.nzx.config.model.Config;
import ru.otr.nzx.config.model.ConfigException;
import ru.otr.nzx.config.model.LocationConfig;
import ru.otr.nzx.config.model.LocationConfigMap;
import ru.otr.nzx.config.model.NZXConfig;
import ru.otr.nzx.config.model.SimpleConfig;
import ru.otr.nzx.config.model.LocationConfig.LocationType;
import ru.otr.nzx.util.NZXUtil;

public class ConfigService {
	public final static String DEFAULT_CONFIG_PATHNAME = "config" + File.separator + "nzx.conf";
	public final static String PROPERTY_NZX_LOG = "nzx_log";
	public final static String SERVICE_NAME = "ConfigService";

	private final Tracer tracer;
	private final NZXConfig nzx;

	private HttpProxyServerBootstrap srvBootstrap;
	private HttpProxyServer srv;

	public ConfigService(File nzxConfigFile, Tracer tracer) throws IOException, ConfigException {
		this.tracer = tracer.getSubtracer(SERVICE_NAME);
		if (nzxConfigFile.exists()) {
			this.tracer.info("NZX.Config.File", nzxConfigFile.getPath());
		} else {
			this.tracer.error("NZX.Config.File.NotFound", nzxConfigFile.getPath());
			throw new FileNotFoundException(nzxConfigFile.getPath());
		}

		String[] lines = new String(Files.readAllBytes(nzxConfigFile.toPath())).split("\n");
		StringBuilder cleanCfg = new StringBuilder();
		for (String line : lines) {
			if (!line.trim().startsWith("//")) {
				cleanCfg.append(line);
				cleanCfg.append("\n");
			}
		}
		try {
			this.nzx = new NZXConfig(new JSONObject(cleanCfg.toString()));
		} catch (JSONException e) {
			throw new ConfigException(e);
		}
		if (nzx.log_config != null) {
			loadLogConfig(nzxConfigFile.getParentFile().getPath() + File.separator + nzx.log_config, nzx.log);
		}
		this.tracer.debug("NZX.Config.Loaded", nzx.toString());
		this.tracer.debug("NZX.Config.Context", nzx.getContext().keySet().toString());
	}

	public void bootstrap() {
		if (nzx.config_service_port > 0) {
			srvBootstrap = DefaultHttpProxyServer.bootstrap().withName(ConfigService.SERVICE_NAME)
			        .withAddress(new InetSocketAddress("localhost", nzx.config_service_port));
			srvBootstrap.withFiltersSource(new HttpFiltersSourceAdapter() {
				public HttpFilters filterRequest(HttpRequest request, ChannelHandlerContext ctx) {
					String requestID = NZXUtil.makeRequestID();
					Date requestDateTime = new Date();
					tracer.info("Request", NZXUtil.requestToLongLine(requestID, request, ctx, tracer.isDebugEnabled()));
					return new ConfigLocation(request, ctx, requestDateTime, requestID, ConfigService.this, tracer);
				}
			});
		}

	}

	public void start() {
		if (nzx.config_service_port > 0) {
			tracer.info("Listen", "localhost:" + nzx.config_service_port);
			srv = srvBootstrap.start();
		}
	}

	public void stop() {
		if (nzx.config_service_port > 0) {
			srv.stop();
		}
	}

	public NZXConfig nzx() {
		return nzx;
	}

	public Map<String, Config> getContext() {
		return nzx.getContext();
	}

	private void loadLogConfig(String logConfigFile, String logDir) {
		tracer.info("Log.Config.File", logConfigFile);
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

	public LocationConfig updateLocation(LocationConfig node, Map<String, String> parameters) throws ConfigException {
		synchronized (node) {
			if (parameters.containsKey(LocationConfig.ENABLE)) {
				node.enable = Boolean.valueOf(parameters.get(LocationConfig.ENABLE));
			}
			if (parameters.containsKey(LocationConfig.PROXY_PASS)) {
				try {
					node.proxy_pass = new URI(parameters.get(LocationConfig.PROXY_PASS));
				} catch (URISyntaxException e) {
					throw new ConfigException(e);
				}
				node.type = LocationType.PROXY_PASS;
			}
			tracer.info("Location.Config.Update", node.getPathName() + "=" + node.toString());
			return node;
		}
	}

	public LocationConfigMap createLocation(LocationConfigMap node, Map<String, String> parameters) throws ConfigException {
		synchronized (node) {
			String name = parameters.get(LocationConfig.NAME);
			if (name == null || name.length() == 0) {
				throw new ConfigException("name cannot be empty!");
			}
			String path = parameters.get(LocationConfig.PATH);
			if (path == null || path.length() == 0) {
				throw new ConfigException("path cannot be empty!");
			}
			try {
				new URI(path);
			} catch (URISyntaxException e) {
				throw new ConfigException(e);
			}
			LocationConfig loc = new LocationConfig(name, path, node);
			tracer.info("Location.Config.Create", loc.getPathName() + "=" + loc.toString());
			return node;
		}
	}

	public void deleteLocation(LocationConfig node) throws ConfigException {
		node.delete();
		tracer.info("Location.Config.Delete", node.getPathName() + "=" + node.toString());
	}

	public ActionConfig updateAction(ActionConfig node, Map<String, String> parameters) throws ConfigException {
		synchronized (node) {
			if (parameters.containsKey(LocationConfig.ENABLE)) {
				node.enable = Boolean.valueOf(parameters.get(ActionConfig.ENABLE));
			}
			tracer.info("Action.Config.Update", node.getPathName() + "=" + node.toString());
			return node;
		}
	}

	public SimpleConfig update(SimpleConfig node, Map<String, String> parameters) throws ConfigException {
		node.putAll(parameters);
		tracer.info(node.getName() + ".Config.Update", node.getPathName() + "=" + node.toString());
		return node;
	}

	public SimpleConfig delete(SimpleConfig node) throws ConfigException {
		node.clear();
		tracer.info(node.getName() + ".Config.Delete", node.getPathName() + "=" + node.toString());
		return node;
	}

}
