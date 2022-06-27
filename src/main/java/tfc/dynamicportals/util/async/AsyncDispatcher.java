package tfc.dynamicportals.util.async;

import java.util.ArrayList;

public class AsyncDispatcher {
	private static final ReusableThread[] threads;
	
	private static final ArrayList<Runnable> runnables = new ArrayList<>();
	
	private static final Object lock = new Object();
	private static boolean dispatcherSelected = false;
	
	static {
		threads = new ReusableThread[4];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ReusableThread(() -> {
			});
		}
	}
	
	public static void dispatch(Runnable runnable) {
		synchronized (runnables) {
			if (countOpenings() == 0) {
				runnables.add(() -> {
					runnable.run();
					boolean isDispatcher = false;
					synchronized (lock) {
						if (!dispatcherSelected) {
							isDispatcher = true;
							dispatcherSelected = true;
						}
					}
					if (isDispatcher) {
						awaitOpening();
						synchronized (runnables) {
							for (int i = 0; i < countOpenings(); i++) {
								if (runnables.isEmpty()) return;
								scheduleNext(runnables.remove(0));
							}
						}
					}
				});
			} else {
				scheduleNext(() -> {
					runnable.run();
					boolean isDispatcher = false;
					synchronized (lock) {
						if (!dispatcherSelected) {
							isDispatcher = true;
							dispatcherSelected = true;
						}
					}
					if (isDispatcher) {
						awaitOpening();
						synchronized (runnables) {
							for (int i = 0; i < countOpenings(); i++) {
								if (runnables.isEmpty()) return;
								scheduleNext(runnables.remove(0));
							}
						}
					}
				});
			}
		}
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
