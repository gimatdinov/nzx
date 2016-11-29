package cxc.jex.postprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import cxc.jex.buffer.ByteBufferPool;
import cxc.jex.server.Server;
import cxc.jex.tracer.Tracer;

public abstract class PostProcessor<T extends Tank> extends Server {
    private ExecutorService executor;
    boolean started = false;

    final ConcurrentLinkedQueue<T> loadedTanks = new ConcurrentLinkedQueue<>();

    protected final List<Action<T>> actions = new ArrayList<>();

    private ByteBufferPool bufferPool;
    private List<Worker<T>> workers = new ArrayList<>();
    private Random random = new Random();

    public PostProcessor(String name, Tracer tracer) {
        super(tracer.getSubtracer(name));
    }

    public void init(int workers, int bufferPoolSize, int bufferSizeMin, List<Action<T>> actions, ThreadFactory threadFactory) {
        bufferPool = new ByteBufferPool(bufferPoolSize, bufferSizeMin);
        executor = Executors.newFixedThreadPool(workers, threadFactory);
        for (int i = 0; i < workers; i++) {
            this.workers.add(new Worker<T>(this));
        }
        for (Action<T> item : actions) {
            this.actions.add(item);
        }
    }

    @Override
    public void start() {
        started = true;
        tracer.info("Starting", "count of workers " + workers.size());
        for (Worker<T> item : workers) {
            executor.submit(item);
        }
    }

    @Override
    public void stop() {
        started = false;
        for (Worker<T> item : workers) {
            item.signal();
        }
        executor.shutdown();
        tracer.info("Stoped", "");
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
