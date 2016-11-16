package ru.otr.nzx.postprocessing;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ru.otr.nzx.postprocessing.PostProcessor.Action;

class Worker implements Runnable {
	private Lock lock = new ReentrantLock();
	private Condition comingLoadedTanks = lock.newCondition();
	private final PostProcessor postProcessor;

	public Worker(PostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	@Override
	public void run() {
		postProcessor.getTracer().info("Starting", "");
		while (postProcessor.started) {
			lock.lock();
			Tank tank = postProcessor.loadedTanks.poll();
			try {
				if (tank != null) {
					for (Action action : postProcessor.actions) {
						action.process(tank, postProcessor.getTracer());
					}
				} else {
					comingLoadedTanks.await();
				}
			} catch (Exception e) {
				postProcessor.getTracer().error("Error/NOTIFY_ADMIN", tank.toString(), e);
			} finally {
				if (tank != null) {
					postProcessor.emptyTanks.add(tank);
					postProcessor.checkEmptyTanks();
				}
				lock.unlock();
			}
		}
		postProcessor.getTracer().info("Stoped", "");
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
