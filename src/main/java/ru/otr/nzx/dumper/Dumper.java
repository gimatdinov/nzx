package ru.otr.nzx.dumper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.DumperConfig;

public class Dumper {
    public static class Tank {
        public String directoryPath;
        public String fileName;
        public final byte[] data;
        public int contentLength;

        Tank(int capacity) {
            data = new byte[capacity];
        }

        public String getFilePath() {
            return directoryPath + "/" + fileName;
        }
    }

    private final DumperConfig config;
    private final Tracer tracer;

    public final Lock lock = new ReentrantLock();
    public final Condition check = lock.newCondition();

    private boolean started;

    private final ConcurrentLinkedQueue<Tank> loadedTankQueue;
    private final ConcurrentLinkedQueue<Tank> emptyTankQueue;

    public Dumper(String name, DumperConfig config, Tracer tracer) {
        this.tracer = tracer.getSubtracer(name);
        this.config = config;
        loadedTankQueue = new ConcurrentLinkedQueue<>();
        emptyTankQueue = new ConcurrentLinkedQueue<>();
    }

    public void start() {
        started = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                tracer.info("Starting", "");
                while (started) {
                    lock.lock();
                    try {
                        Tank tank = loadedTankQueue.poll();
                        if (tank != null) {
                            emptyTankQueue.add(dump(tank));
                        } else {
                            check.await();
                        }
                    } catch (Exception e) {
                        tracer.error("Error/NOTIFY_ADMIN", e.getMessage(), e);
                    } finally {
                        lock.unlock();
                    }
                }
                tracer.info("Stoped", "");
            }

        }).start();
    }

    public void stop() {
        lock.lock();
        try {
            started = false;
            check.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public Tank getTank() {
        Tank result = emptyTankQueue.poll();
        if (result == null) {
            result = new Tank(config.tank_capacity);
            tracer.debug("Tank.Count", "" + (loadedTankQueue.size() + emptyTankQueue.size() + 1));
        }
        return result;
    }

    public void add(Tank loadedTank) {
        if (!started) {
            tracer.error("Error", "Not started!");
            return;
        }
        loadedTankQueue.add(loadedTank);
    }

    private Tank dump(Tank tank) {
        tracer.debug("Dump", "file=[" + tank.getFilePath() + "] size=" + tank.contentLength);
        File dir = new File(tank.directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(tank.getFilePath())) {
            fos.write(tank.data, 0, tank.contentLength);
        } catch (IOException e) {
            tracer.error("Dump.Error/NOTIFY_ADMIN", "file=[" + tank.getFilePath() + "]", e);
        }
        return tank;
    }

    public boolean isStarted() {
        return started;
    }

}
