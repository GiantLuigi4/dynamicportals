package tfc.dynamicportals.mixin.common.access;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.access.ConnectionAccessor;

@Mixin(Connection.class)
public abstract class ConnectionMixin implements ConnectionAccessor {
	@Shadow
	protected abstract void channelRead0(ChannelHandlerContext par1, Object par2) throws Exception;

	@Shadow
	protected abstract void tickSecond();

	@Override
	public void invokeRead0(ChannelHandlerContext p_129487_, Packet<?> p_129488_) throws Exception {
		channelRead0(p_129487_, p_129487_);
	}

	@Override
	public void invokeTickSecond() {
		tickSecond();
	}
}
