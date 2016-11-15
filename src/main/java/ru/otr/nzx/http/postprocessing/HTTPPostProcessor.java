package ru.otr.nzx.http.postprocessing;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.Server;
import ru.otr.nzx.config.http.postprocessing.ActionConfig;
import ru.otr.nzx.config.http.postprocessing.HTTPPostProcessorConfig;

public class HTTPPostProcessor extends Server {
	public static interface Action {
		void process(Tank tank, Tracer tracer);
	}

	private final HTTPPostProcessorConfig config;
	private final int bufferSize;

	public final Lock lock = new ReentrantLock();
	public final Condition check = lock.newCondition();

	private boolean started;

	private final ConcurrentLinkedQueue<Tank> loadedTankQueue;
	private final ConcurrentLinkedQueue<Tank> emptyTankQueue;

	private final List<Action> actions;

	public HTTPPostProcessor(String name, HTTPPostProcessorConfig config, int bufferSize, Tracer tracer) {
		super(tracer.getSubtracer(name));
		this.config = config;
		this.bufferSize = bufferSize;
		loadedTankQueue = new ConcurrentLinkedQueue<>();
		emptyTankQueue = new ConcurrentLinkedQueue<>();
		actions = new ArrayList<>();
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
			Object action = actionConstrct.newInstance(actionConfig.parameters);
			registerAction((Action) action);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void bootstrap() {
		registerAction(new Dumping());
		for (ActionConfig item : config.actions) {
			loadActions(item);
		}
	}

	@Override
	public void start() {
		started = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				tracer.info("Starting", "");
				while (started) {
					lock.lock();
					Tank tank = loadedTankQueue.poll();
					try {
						if (tank != null) {
							for (Action action : actions) {
								action.process(tank, tracer);
							}
						} else {
							check.await();
						}
					} catch (Exception e) {
						tracer.error("Error/NOTIFY_ADMIN", tank.toString(), e);
					} finally {
						if (tank != null) {
							emptyTankQueue.add(tank);
						}
						lock.unlock();
					}
				}
				tracer.info("Stoped", "");
			}
		}).start();
	}

	@Override
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
			result = new Tank(bufferSize);
			tracer.debug("Tank.Created", "count of tanks ~ " + (loadedTankQueue.size() + emptyTankQueue.size() + 1));
		}
		return result;
	}

	public void put(Tank loadedTank) {
		if (!started) {
			tracer.error("Error", "Not started!");
			return;
		}
		loadedTankQueue.add(loadedTank);
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
}
