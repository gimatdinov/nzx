package cxc.jex.postprocessing;

import cxc.jex.tracer.Tracer;

public interface Action {
    void process(Tank tank, Tracer tracer);
}