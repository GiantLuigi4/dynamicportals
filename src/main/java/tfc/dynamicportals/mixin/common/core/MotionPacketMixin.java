package tfc.dynamicportals.mixin.common.core;

import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.util.WhyCantMixinsBeHotswapped;

@Mixin(ServerboundMovePlayerPacket.class)
public abstract class MotionPacketMixin {
	@Inject(at = @At("HEAD"), method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V", cancellable = true)
	public void preHandle(ServerGamePacketListener pHandler, CallbackInfo ci) {
		WhyCantMixinsBeHotswapped.handleMotionPacket((ServerboundMovePlayerPacket) (Object) this, pHandler, ci);
	}
}
