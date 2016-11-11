package ru.otr.nzx;

import cxc.jex.tracer.Tracer;

public abstract class Server {
    protected final Tracer tracer;

    public Server(Tracer tracer) {
        this.tracer = tracer;
    }

    public abstract void bootstrap();

    public abstract void start();

    public abstract void stop();

}
