package tfc.dynamicportals.api.implementation.data;

import net.minecraft.network.syncher.EntityDataSerializer;

public class PortalTrackedData<T> {
	String name;
	EntityDataSerializer<T> serializer;
	
	public PortalTrackedData(String name, EntityDataSerializer<T> serializer) {
		this.name = name;
		this.serializer = serializer;
	}
}
