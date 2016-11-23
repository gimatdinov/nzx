package cxc.jex.postprocessing;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Worker implements Runnable {
	private Lock lock = new ReentrantLock();
	private Condition comingLoadedTanks = lock.newCondition();
	private final PostProcessor postProcessor;

	public Worker(PostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	@Override
	public void run() {
		postProcessor.getTracer().info("Worker.Starting", "");
		while (postProcessor.started.get()) {
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
				postProcessor.getTracer().error("Worker.Error/NOTIFY_ADMIN", tank.toString(), e);
			} finally {
				if (tank != null) {
					postProcessor.emptyTanks.add(tank);
					postProcessor.checkEmptyTanks();
				}
				lock.unlock();
			}
		}
		postProcessor.getTracer().info("Worker.Stoped", "");
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
