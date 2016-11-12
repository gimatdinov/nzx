package cxc.jex.tracer;

import java.util.List;

public interface Tracer extends TracerLogger {

    public String getName();

    public List<String> getPath();

    public Tracer getSubtracer(String name);

}
