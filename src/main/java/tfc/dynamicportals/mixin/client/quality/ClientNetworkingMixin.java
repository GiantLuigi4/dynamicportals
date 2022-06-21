package tfc.dynamicportals.mixin.client.quality;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.access.IMaySkipPacket;

@Mixin(ClientPacketListener.class)
public class ClientNetworkingMixin {
	@Shadow @Final private Connection connection;
	
	@Inject(at = @At("HEAD"), method = "handleMovePlayer", cancellable = true)
	public void preTeleport(ClientboundPlayerPositionPacket pPacket, CallbackInfo ci) {
		Entity e = Minecraft.getInstance().player;
		if (e != null) {
			if (((IMaySkipPacket) e).skip()) {
				double x = pPacket.getX();
				double y = pPacket.getY();
				double z = pPacket.getZ();
				e.absMoveTo(x, y, z, e.getYRot(), e.getXRot());
				
				this.connection.send(new ServerboundAcceptTeleportationPacket(pPacket.getId()));
//				this.connection.send(new ServerboundMovePlayerPacket.PosRot(e.getX(), e.getY(), e.getZ(), e.getYRot(), e.getXRot(), false));
				
				ci.cancel();
			}
		}
	}
}
