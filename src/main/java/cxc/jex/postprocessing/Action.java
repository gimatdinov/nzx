package cxc.jex.postprocessing;

import cxc.jex.tracer.Tracer;

public interface Action<T extends Tank> {
    void process(T tank, Tracer tracer);
}