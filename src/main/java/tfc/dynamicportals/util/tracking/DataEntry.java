package tfc.dynamicportals.util.tracking;

import tfc.dynamicportals.api.implementation.data.PortalTrackedData;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DataEntry<T> {
	public final PortalTrackedData<T> parameter;
	public final Supplier<T> getter;
	public final Consumer<T> updater;
	boolean dirty = true;
	
	public DataEntry(PortalTrackedData<T> position, Supplier<T> getter, Consumer<T> updater) {
		this.parameter = position;
		this.getter = getter;
		this.updater = updater;
	}
	
	public DataEntry<T> setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
	public boolean isDirty() {
		return dirty;
	}
}
