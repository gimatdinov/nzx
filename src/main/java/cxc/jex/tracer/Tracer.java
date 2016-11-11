package cxc.jex.tracer;

import java.util.List;

public interface Tracer {

    public String getName();

    public List<String> getPath();

    public Tracer getSubtracer(String name);

    public void trace(String event, String msg);

    public void trace(String event, String msg, Throwable t);

}
