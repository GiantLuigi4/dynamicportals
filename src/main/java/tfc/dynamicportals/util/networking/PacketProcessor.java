package tfc.dynamicportals.util.networking;

import net.minecraft.nbt.CompoundTag;

public abstract class PacketProcessor {
	private String registryName;
	
	void setRegistryName(String registryName) {
		this.registryName = registryName;
	}
	
	public String registryName() {
		return registryName;
	}
	
	public abstract CompoundTag serialize();
	
	public abstract void deserialize(CompoundTag readNbt);
}
