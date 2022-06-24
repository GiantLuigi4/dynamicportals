package tfc.dynamicportals.access;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.Packet;

public interface ConnectionAccessor {
	void invokeRead0(ChannelHandlerContext p_129487_, Packet<?> p_129488_) throws Exception;

	void invokeTickSecond();
}
