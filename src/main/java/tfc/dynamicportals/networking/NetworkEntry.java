package tfc.dynamicportals.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class NetworkEntry<T extends Packet> {
	Class<T> clazz;
	Function<FriendlyByteBuf, T> fabricator;
	
	public NetworkEntry(Class<T> clazz, Function<FriendlyByteBuf, T> fabricator) {
		this.clazz = clazz;
		this.fabricator = fabricator;
	}
	
	public void register(int indx, SimpleChannel channel) {
		channel.registerMessage(
				indx, clazz,
				Packet::write,
				fabricator,
				(pkt, ctx) -> pkt.handle(ctx.get())
		);
	}
}

