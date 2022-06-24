package tfc.dynamicportals.util.networking;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class NetSchenanigans {
	private static final AtomicReference<ArrayList<PacketProcessor>> processors = new AtomicReference<>();

	private static void ensureNotNull() {
		if (processors.get() == null) {
			processors.set(new ArrayList<>());
		}
	}

	public static void addProcessor(PacketProcessor processor) {
		ensureNotNull();
		processors.get().add(processor);
	}

	public static void backup() {
		ensureNotNull();
		processors.get().remove(processors.get().size() - 1);
	}

	public static ArrayList<PacketProcessor> getStack() {
		ensureNotNull();
		return processors.get();
	}
}
