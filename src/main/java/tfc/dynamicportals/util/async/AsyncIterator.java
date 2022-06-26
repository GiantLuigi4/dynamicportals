package tfc.dynamicportals.util.async;

import java.util.function.Consumer;

public class AsyncIterator {
	private static final ReusableThread[] threads;
	
	static {
		threads = new ReusableThread[4];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ReusableThread(() -> {
			});
		}
	}
	
	public static <T> void forEach(Iterable<T> iterable, Consumer<T> function) {
		for (T t : iterable) {
			scheduleNext(() -> {
				function.accept(t);
			});
		}
		for (ReusableThread thread : threads) thread.await();
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
}
