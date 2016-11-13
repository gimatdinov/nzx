package cxc.jex.tracer;

import java.util.List;

public interface Tracer extends TracerLogger {

    String getName();

    List<String> getPath();

    Tracer getSubtracer(String name);

}
