package ru.otr.nzx.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.littleshoot.proxy.HttpFiltersAdapter;

import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.config.http.location.LocationConfig.LocationType;
import ru.otr.nzx.config.postprocessing.ActionConfig;
import ru.otr.nzx.util.NZXUtil;

public class ConfigLocation extends HttpFiltersAdapter {
	private final Tracer tracer;
	private final NZXConfigService cfgService;
	private URI cfgURI;

	public ConfigLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, NZXConfigService cfgService,
	        Tracer tracer) {
		super(originalRequest, ctx);
		this.cfgService = cfgService;
		this.tracer = tracer;
		try {
			URI uri = new URI(originalRequest.getUri()).normalize();
			cfgURI = new URI(uri.getPath());
			if (uri.getRawQuery() != null) {
				cfgURI = new URI(cfgURI.toString() + "?" + uri.getRawQuery()).normalize();
			}
		} catch (URISyntaxException e) {
			tracer.error("Make.ConfigURI", e.getMessage(), e);
			cfgURI = null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HttpResponse clientToProxyRequest(HttpObject httpObject) {
		if (cfgURI == null) {
			return NZXUtil.makeFailureResponse(500, HttpVersion.HTTP_1_1);
		}
		if (httpObject instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) httpObject;
			Object node = cfgService.getRoutes().get(cfgURI.getPath());
			if (HttpMethod.GET.equals(request.getMethod())) {
				tracer.info("GET", cfgURI.getPath());

				if (node != null) {
					if (node instanceof Config) {
						return NZXUtil.configToHttpResponse((Config) node);
					}
					if (node instanceof List) {
						return NZXUtil.configsToHttpResponse((List<? extends Config>) node);
					}
					if (node instanceof Map) {
						return NZXUtil.configsToHttpResponse(((Map<String, ? extends Config>) node).values());
					}
				}
			}
			if (HttpMethod.PUT.equals(request.getMethod())) {
				tracer.info("PUT", cfgURI.toString());
				try {
					if (node != null) {
						if (node instanceof Config) {
							Config cfg = (Config) node;
							if (cfg instanceof LocationConfig) {
								update((LocationConfig) cfg, URLEncodedUtils.parse(cfgURI, "UTF-8"));
							}
							if (node instanceof ActionConfig) {
								update((ActionConfig) cfg, URLEncodedUtils.parse(cfgURI, "UTF-8"));
							}
							return NZXUtil.configToHttpResponse(cfg);
						}
						if (node instanceof Map) {
							Map map = (Map) node;
							if (map.size() > 0) {
								if (map.values().iterator().next().getClass().isAssignableFrom(LocationConfig.class)) {
									createNewLocation(cfgURI.getPath(), map, URLEncodedUtils.parse(cfgURI, "UTF-8"));
								}
							}
							return NZXUtil.configsToHttpResponse(map.values());
						}
					}
				} catch (Exception e) {
					return NZXUtil.makeSimpleResponse(e.getMessage(), "text/plain", 400, HttpVersion.HTTP_1_1);
				}
			}
		}
		return NZXUtil.makeSimpleResponse("non existent", "text/plain", 404, HttpVersion.HTTP_1_1);
	}

	private void update(LocationConfig cfg, List<NameValuePair> params) throws URISyntaxException {
		synchronized (cfg) {
			for (NameValuePair item : params) {
				if (item.getName().equals(LocationConfig.ENABLE)) {
					cfg.enable = Boolean.valueOf(item.getValue());
				}
				if (cfg.type == LocationType.FILE) {
					break;
				}
				if (item.getName().equals(LocationConfig.PROXY_PASS)) {
					cfg.proxy_pass = new URI(item.getValue());
					cfg.type = LocationType.PROXY_PASS;
				}
			}
		}
	}

	private void update(ActionConfig cfg, List<NameValuePair> params) throws URISyntaxException {
		synchronized (cfg) {
			Map<String, String> parameters = new HashMap<>();
			for (NameValuePair item : params) {
				parameters.put(item.getName(), item.getValue());
			}
			cfg.setParameters(parameters);
			cfg.getAction().loadParameters();
			cfg.getAction().setEnable(cfg.enable);
		}
	}

	private void createNewLocation(String route, Map<String, LocationConfig> locations, List<NameValuePair> params) throws URISyntaxException {
		URI path = null;
		for (NameValuePair item : params) {
			if (item.getName().equals(LocationConfig.PATH)) {
				path = new URI(item.getValue()).normalize();
			}
		}
		if (path == null) {
			throw new IllegalArgumentException("path cannot be empty!");
		}
		for (LocationConfig item : locations.values()) {
			item.unregisterRoute();
		}
		int i = 0;
		for (LocationConfig item : locations.values()) {
			item.index = i++;
			cfgService.getRoutes().put(route + "/" + item.index, item);
		}
		locations.put(path.toString(), new LocationConfig(locations.size(), path.toString(), route, cfgService.getRoutes()));

	}
	
	private void delete(LocationConfig cfg) {
		cfg.unregisterRoute();
		
	}
}
