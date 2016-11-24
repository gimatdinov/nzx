package cxc.jex.postprocessing;

import cxc.jex.tracer.Tracer;

public interface Action<T> {
    void process(T tank, Tracer tracer);
}