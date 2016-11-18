package cxc.jex.postprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cxc.jex.server.Server;
import cxc.jex.tracer.Tracer;

public abstract class PostProcessor extends Server {
    private Lock lock = new ReentrantLock();
    private Condition comingEmptyTanks = lock.newCondition();

    private ExecutorService executor;

    boolean started;
    final ConcurrentLinkedQueue<Tank> loadedTanks = new ConcurrentLinkedQueue<>();
    final ConcurrentLinkedQueue<Tank> emptyTanks = new ConcurrentLinkedQueue<>();
    protected final List<Action> actions = new ArrayList<>();

    private int maxTanksCount;
    private List<Worker> workers = new ArrayList<>();
    private Random random = new Random();

    public PostProcessor(String name, Tracer tracer) {
        super(tracer.getSubtracer(name));
    }

    public void init(int workers, int maxTanksCount, List<Action> actions, ThreadFactory threadFactory) {
        this.maxTanksCount = maxTanksCount;
        executor = Executors.newFixedThreadPool(workers, threadFactory);
        for (int i = 0; i < workers; i++) {
            this.workers.add(new Worker(this));
        }
        for (Action item : actions) {
            this.actions.add(item);
        }
    }

    @Override
    public void start() {
        started = true;
        tracer.info("Starting", "count of workers " + workers.size());
        for (Worker item : workers) {
            executor.submit(item);
        }
    }

    @Override
    public void stop() {
        started = false;
        for (Worker item : workers) {
            item.signal();
        }
        executor.shutdown();
        tracer.info("Stoped", "");
    }

    protected abstract Tank makeTank();

    public Tank getTank(int contentLength) {
        Tank result = emptyTanks.poll();
        if (result == null) {
            int tanksCount = loadedTanks.size() + emptyTanks.size();
            if (tanksCount < maxTanksCount || maxTanksCount == 0) {
                result = makeTank();
                tracer.info("Tank.Created", "count of tanks ~ " + (tanksCount + 1));
            } else {
                tracer.info("Tank.Awaiting", "count of tanks ~ " + (tanksCount + 1));
                while (true) {
                    lock.lock();
                    try {
                        result = emptyTanks.poll();
                        if (result != null) {
                            break;
                        } else {
                            comingEmptyTanks.await();
                        }
                    } catch (InterruptedException e) {
                        tracer.error("Error/NOTIFY_ADMIN", e.getMessage(), e);
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
        return result;
    }

    public void put(Tank loadedTank) {
        if (!started) {
            tracer.error("Error", "Not started!");
            return;
        }
        int wix = (int) Math.round((workers.size() - 1) * random.nextDouble());
        Worker worker = workers.get(wix);
        loadedTanks.add(loadedTank);
        worker.signal();

    }

    public boolean isStarted() {
        return started;
    }

    Tracer getTracer() {
        return tracer;
    }

    void checkEmptyTanks() {
        lock.lock();
        try {
            comingEmptyTanks.signal();
        } finally {
            lock.unlock();
        }
    }
}
