package tfc.dynamicportals.util.networking;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.function.Supplier;

public class ProcessorRegistry {
	static final HashMap<String, Supplier<PacketProcessor>> registry = new HashMap<>();

	static {
		register(
				"dynamicportals:worldredir",
				() -> new PacketProcessor() {
					@Override
					public CompoundTag serialize() {
						return new CompoundTag();
					}

					@Override
					public void deserialize(CompoundTag readNbt) {
					}
				}
		);
	}

	static void register(String name, Supplier<PacketProcessor> processorSupplier) {
		registry.put(name, () -> {
			PacketProcessor processor = processorSupplier.get();
			processor.setRegistryName(name);
			return processor;
		});
	}
}
