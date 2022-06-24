package tfc.dynamicportals.util.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class WrapperPacket implements Packet {
	ArrayList<PacketProcessor> processors = new ArrayList<>();
	Packet wrapped;

	public WrapperPacket(Packet wrapped) {
		this.wrapped = wrapped;
		for (PacketProcessor packetProcessor : NetSchenanigans.getStack()) {
			processors.add(packetProcessor);
		}
	}

	public WrapperPacket(FriendlyByteBuf buf) {
		for (int i = 0; i < buf.readInt(); i++) {
			String regName = buf.readUtf();
			PacketProcessor processor = ProcessorRegistry.registry.get(regName).get();
			processor.deserialize(buf.readNbt());
			processors.add(processor);
		}
		try {
			Class<?> clazz = Class.forName(buf.readUtf());
			Constructor<?> ctor = clazz.getConstructor(FriendlyByteBuf.class);
			wrapped = (Packet) ctor.newInstance(buf);
		} catch (Throwable ignored) {
		}
	}

	@Override
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeInt(processors.size());
		for (PacketProcessor processor : processors) {
			pBuffer.writeUtf(processor.registryName());
			pBuffer.writeNbt(processor.serialize());
		}
		pBuffer.writeUtf(wrapped.getClass().getName());
		wrapped.write(pBuffer);
	}

	@Override
	public void handle(PacketListener pHandler) {

	}
}
