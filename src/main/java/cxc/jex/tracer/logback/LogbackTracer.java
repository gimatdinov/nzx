package cxc.jex.tracer.logback;

import java.util.ArrayList;
import java.util.List;

import cxc.jex.tracer.Tracer;

public class LogbackTracer extends LogbackTracerLogger implements Tracer {
    private final String name;
    private final List<String> path;

    private static List<String> makePath(String name, LogbackTracer host) {
        List<String> result = new ArrayList<>();
        if (host != null) {
            result.addAll(host.path);
        }
        result.add(name);
        return result;
    }

    public LogbackTracer(String name) {
        super(makePath(name, null).toString());
        this.name = name;
        path = makePath(name, null);
    }

    private LogbackTracer(String name, LogbackTracer host) {
        super(makePath(name, host).toString());
        this.name = name;
        path = makePath(name, host);
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

}
