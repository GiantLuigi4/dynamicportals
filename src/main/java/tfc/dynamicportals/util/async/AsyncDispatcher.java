package tfc.dynamicportals.util.async;

import java.util.ArrayList;

public class AsyncDispatcher {
	private static final ReusableThread[] threads;
	private static final ArrayList<Runnable> runnables = new ArrayList<>();
	
	
	private static final ReusableThread schedulerThread = new ReusableThread(() -> {
		while (true) {
			if (runnables.isEmpty()) {
				try {
					Thread.sleep(1);
				} catch (Throwable ignored) {
				}
			} else {
				try {
					scheduleNext(runnables.remove(0));
				} catch (Throwable ignored) {
				}
			}
		}
	});
	private static final Object lock = new Object();
	private static final boolean dispatcherSelected = false;
	
	static {
		schedulerThread.start();
	}
	
	static {
		threads = new ReusableThread[16];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ReusableThread(() -> {
			});
		}
	}
	
	public static void dispatch(Runnable runnable) {
		if (countOpenings() == 0) runnables.add(runnable);
		else scheduleNext(runnable);
//		synchronized (runnables) {
//			if (countOpenings() == 0) runnables.add(() -> exec(runnable));
//			else scheduleNext(() -> exec(runnable));
//		}
	}
	
	private static int countOpenings() {
		int cnt = 0;
		for (ReusableThread thread : threads)
			if (!thread.isRunning())
				cnt++;
		return cnt;
	}
	
	private static void awaitOpening() {
		while (true) {
			for (ReusableThread thread : threads) {
				if (thread.isRunning()) {
					try {
						Thread.sleep(1);
					} catch (Throwable ignored) {
					}
				} else return;
			}
		}
	}
	
	private static void scheduleNext(Runnable r) {
		while (true) {
			for (ReusableThread thread : threads) {
				if (!thread.isRunning()) {
					thread.setAction(r);
					thread.start();
					return;
				}
			}
		}
	}
	
	public static void await() {
		while (true) {
			while (!runnables.isEmpty()) {
				try {
					Thread.sleep(1);
				} catch (Throwable ignored) {
				}
			}
			for (ReusableThread thread : threads) {
				if (thread.isRunning()) {
					try {
						Thread.sleep(1);
					} catch (Throwable ignored) {
					}
				} else return;
			}
		}
	}
}
