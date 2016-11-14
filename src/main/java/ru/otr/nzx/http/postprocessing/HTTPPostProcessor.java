package ru.otr.nzx.http.postprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.HTTPPostProcessorConfig;

public class HTTPPostProcessor {
	public static interface Action {
		void process(Tank tank, Tracer tracer);
	}

	private final HTTPPostProcessorConfig config;
	private final Tracer tracer;

	public final Lock lock = new ReentrantLock();
	public final Condition check = lock.newCondition();

	private boolean started;

	private final ConcurrentLinkedQueue<Tank> loadedTankQueue;
	private final ConcurrentLinkedQueue<Tank> emptyTankQueue;

	private final List<Action> actions;

	public HTTPPostProcessor(String name, HTTPPostProcessorConfig config, Tracer tracer) {
		this.tracer = tracer.getSubtracer(name);
		this.config = config;
		loadedTankQueue = new ConcurrentLinkedQueue<>();
		emptyTankQueue = new ConcurrentLinkedQueue<>();
		actions = new ArrayList<>();
	}

	public void registerAction(Action action) {
		actions.add(action);
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
							for (Action action : actions) {
								action.process(tank, tracer);
							}
							emptyTankQueue.add(tank);
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

	public boolean isStarted() {
		return started;
	}

	public boolean dumping() {
		for (Action action : actions) {
			if (action instanceof Dumping) {
				return true;
			}
		}
		return false;
	}

}
