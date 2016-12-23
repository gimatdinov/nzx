package cxc.jex.postprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import cxc.jex.buffer.ByteBufferPool;
import cxc.jex.tracer.Tracer;

public abstract class PostProcessor<T extends Tank> {
    protected final Tracer tracer;
    private ExecutorService executor;
    boolean started = false;

    protected final ConcurrentLinkedQueue<T> loadedTanks = new ConcurrentLinkedQueue<>();

    protected final List<Action<T>> actions = new ArrayList<>();

    private ByteBufferPool bufferPool;
    private List<Worker<T>> workers = new ArrayList<>();
    private Random random = new Random();

    public PostProcessor(int workers, List<Action<T>> actions, ThreadFactory threadFactory, Tracer tracer) {
        this.tracer = tracer;
        if (actions != null) {
            this.actions.addAll(actions);
        }
        for (int i = 0; i < workers; i++) {
            this.workers.add(new Worker<T>(this));
        }
        this.executor = Executors.newFixedThreadPool(workers, threadFactory);
    }

    public void bootstrap(int bufferPoolSize, int bufferSizeMin) {
        tracer.info("Bootstrap", "");
        this.bufferPool = new ByteBufferPool(bufferPoolSize, bufferSizeMin);
        this.tracer.info("BufferPool.Created", this.bufferPool.getSpaceSize() + " bytes, " + this.bufferPool.getLevels() + " levels");
    }

    public void start() {
        started = true;
        tracer.info("Starting", "count of workers " + workers.size());
        for (Worker<T> item : workers) {
            executor.submit(item);
        }
    }

    public void stop() {
        started = false;
        for (Worker<T> item : workers) {
            item.signal();
        }
        executor.shutdown();
        tracer.info("Stopped", "");
    }

    public void attachBuffer(T tank, int capacity) {
        tank.setBuffer(bufferPool.borrow(capacity));
    }

    public void put(T loadedTank) {
        if (!started) {
            tracer.error("Error/NOTIFY_ADMIN", "Not started!");
            return;
        }
        int wix = (int) Math.round((workers.size() - 1) * random.nextDouble());
        Worker<T> worker = workers.get(wix);
        loadedTanks.add(loadedTank);
        worker.signal();

    }

    public boolean isStarted() {
        return started;
    }

    Tracer getTracer() {
        return tracer;
    }

}
