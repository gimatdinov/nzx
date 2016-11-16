package ru.otr.nzx.postprocessing;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.Server;
import ru.otr.nzx.config.postprocessing.ActionConfig;
import ru.otr.nzx.config.postprocessing.PostProcessorConfig;

public class PostProcessor extends Server {
    public static interface Action {
        void process(Tank tank, Tracer tracer);
    }

    private Lock lock = new ReentrantLock();
    private Condition comingEmptyTanks = lock.newCondition();

    private final PostProcessorConfig config;
    private final int bufferSize;

    private ExecutorService executor;

    boolean started;
    final ConcurrentLinkedQueue<Tank> loadedTanks = new ConcurrentLinkedQueue<>();
    final ConcurrentLinkedQueue<Tank> emptyTanks = new ConcurrentLinkedQueue<>();
    final List<Action> actions = new ArrayList<>();

    private List<Worker> workers = new ArrayList<>();
    private Random random = new Random();

    public PostProcessor(String name, PostProcessorConfig config, int bufferSize, Tracer tracer) {
        super(tracer.getSubtracer(name));
        this.config = config;
        this.bufferSize = bufferSize;
    }

    public void registerAction(Action action) {
        actions.add(action);
    }

    private void loadActions(ActionConfig actionConfig) {
        try {
            Class<?> actionClass = Class.forName(actionConfig.clazz);
            Class<?>[] paramTypes = new Class[actionConfig.parameters.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = String.class;
            }
            Constructor<?> actionConstrct = actionClass.getConstructor(paramTypes);
            Object action = actionConstrct.newInstance((Object[]) actionConfig.parameters);
            registerAction((Action) action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bootstrap() {
        executor = Executors.newFixedThreadPool(config.workers, new ThreadFactoryBuilder().setNameFormat("nzx-PostProcessor-Worker-%d").build());
        for (int i = 0; i < config.workers; i++) {
            workers.add(new Worker(this));
        }
        registerAction(new Dumping());
        for (ActionConfig item : config.actions) {
            loadActions(item);
        }
    }

    @Override
    public void start() {
        started = true;
        tracer.info("Starting", "count of workers " + config.workers);
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

    public Tank getTank() {
        Tank result = emptyTanks.poll();
        if (result == null) {
            int tanksCount = loadedTanks.size() + emptyTanks.size();
            if (tanksCount < config.max_count_of_tanks || config.max_count_of_tanks == 0) {
                result = new Tank(bufferSize);
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

    public boolean isDumpingEnable() {
        for (Action action : actions) {
            if (action instanceof Dumping) {
                return true;
            }
        }
        return false;
    }

    Tracer getTracer() {
        return tracer;
    }

    public void checkEmptyTanks() {
        lock.lock();
        try {
            comingEmptyTanks.signal();
        } finally {
            lock.unlock();
        }
    }
}
