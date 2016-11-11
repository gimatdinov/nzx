package cxc.jex.tracer.logback;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import cxc.jex.tracer.Tracer;

public class LogbackTracer implements Tracer {
    private final static Logger log = LoggerFactory.getLogger(LogbackTracer.class);

    private final String name;
    private final List<String> path;

    public LogbackTracer(String name) {
        this.name = name;
        path = new ArrayList<>();
        path.add(name);
    }

    private LogbackTracer(String name, LogbackTracer host) {
        this.name = name;
        path = new ArrayList<>(host.path);
        path.add(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getPath() {
        return new ArrayList<>(path);
    }

    @Override
    public Tracer getSubtracer(String name) {
        return new LogbackTracer(name, this);
    }

    @Override
    public void trace(String event, String msg) {
        String[] parts = event.split("/");
        if (parts.length == 2) {
            Marker marker = MarkerFactory.getDetachedMarker(parts[1]);
            log.trace(marker, path + "(" + event + ") " + msg);
        } else {
            log.trace(path + "(" + event + ") " + msg);
        }
    }

    @Override
    public void trace(String event, String msg, Throwable t) {
        String[] parts = event.split("/");
        if (parts.length == 2) {
            Marker marker = MarkerFactory.getDetachedMarker(parts[1]);
            log.trace(marker, path + "(" + event + ") " + msg, t);
        } else {
            log.trace(path + "(" + event + ") " + msg, t);
        }
    }

}
