package cxc.jex.postprocessing;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Worker<T extends Tank> implements Runnable {
    private Lock lock = new ReentrantLock();
    private Condition comingLoadedTanks = lock.newCondition();
    private final PostProcessor<T> postProcessor;

    public Worker(PostProcessor<T> postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Override
    public void run() {
        postProcessor.getTracer().info("Worker.Running", "");
        while (postProcessor.started) {
            lock.lock();
            T tank = postProcessor.loadedTanks.poll();
            try {
                if (tank != null) {
                    for (PostProcessor.Action<T> action : postProcessor.actions) {
                        if (action.isEnable()) {
                            try {
                                action.process(tank, postProcessor.getTracer());
                            } catch (Exception e) {
                                postProcessor.getTracer().error("Action.Error/NOTIFY_ADMIN", action.getClass().getName(), e);
                            }
                        }
                    }
                } else {
                    comingLoadedTanks.await();
                }
            } catch (Exception e) {
                postProcessor.getTracer().error("Worker.Error/NOTIFY_ADMIN", tank.toString(), e);
            } finally {
                if (tank != null) {
                    tank.getBuffer().release();
                }
                lock.unlock();
            }
        }
        postProcessor.getTracer().info("Worker.Stopped", "");
    }

    public void signal() {
        lock.lock();
        try {
            comingLoadedTanks.signal();
        } finally {
            lock.unlock();
        }
    }

}
