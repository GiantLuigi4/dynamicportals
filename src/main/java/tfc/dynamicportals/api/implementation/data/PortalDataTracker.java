package tfc.dynamicportals.api.implementation.data;

import net.minecraft.network.FriendlyByteBuf;
import tfc.dynamicportals.util.tracking.DataEntry;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PortalDataTracker {
	HashMap<String, DataEntry<?>> entries = new HashMap<>();
	boolean updating = false;
	boolean isDirty;
	
	public <T> void register(PortalTrackedData<T> parameter, Supplier<T> getter, Consumer<T> updater) {
		entries.put(parameter.name, new DataEntry<>(parameter, getter, updater));
	}
	
	public void read(FriendlyByteBuf buf) {
		updating = true;
		int countUpdated = buf.readInt();
		for (int i = 0; i < countUpdated; i++) {
			String text = buf.readUtf(); // TODO: use integer keys
			//noinspection unchecked
			DataEntry<Object> entry = (DataEntry<Object>) entries.get(text);
			entry.updater.accept(entry.parameter.serializer.read(buf));
			entry.setDirty(false);
		}
		updating = false;
	}
	
	public <T> void update(PortalTrackedData<T> parameter) {
		if (!updating) {
			//noinspection unchecked
			DataEntry<T> entry = (DataEntry<T>) entries.get(parameter.name);
			entry.setDirty(true);
			isDirty = true;
		}
	}
	
	public void write(FriendlyByteBuf buf) {
		if (buf != null) {
			int count = 0;
			DataEntry<Object>[] entriesUpdated = new DataEntry[entries.size()];
			for (DataEntry<?> value : entries.values()) {
				if (value.isDirty()) {
					entriesUpdated[count++] = (DataEntry<Object>) value;
					value.setDirty(false);
				}
			}
			
			buf.writeInt(count);
			for (int i = 0; i < count; i++) {
				buf.writeUtf(entriesUpdated[i].parameter.name);
				entriesUpdated[i].parameter.serializer.write(buf, entriesUpdated[i].getter.get());
			}
		}
		
		isDirty = false;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
}
