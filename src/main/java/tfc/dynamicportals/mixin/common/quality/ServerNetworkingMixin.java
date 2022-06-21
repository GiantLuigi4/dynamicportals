package tfc.dynamicportals.mixin.common.quality;

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.TeleportationHandler;
import tfc.dynamicportals.access.IMaySkipPacket;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerNetworkingMixin {
	@Shadow
	public ServerPlayer player;
	
	@Shadow
	private double lastGoodX;
	
	@Shadow
	private double lastGoodY;
	
	@Shadow
	private double lastGoodZ;

//	@Unique
//	private boolean doSkip = false;
//
//	@Inject(at = @At("TAIL"), method = "teleport(DDDFFLjava/util/Set;Z)V", cancellable = true)
//	public void postTeleport(double p_143618_, double p_143619_, double p_143620_, float p_143621_, float p_143622_, Set<ClientboundPlayerPositionPacket.RelativeArgument> p_143623_, boolean p_143624_, CallbackInfo ci) {
//		if (doSkip) {
//			awaitingPositionFromClient = null;
//		}
//	}

	@Shadow
	private int receivedMovePacketCount;
	
	@Shadow
	@Final
	private static Logger LOGGER;
	
	@Shadow
	private int knownMovePacketCount;
	
	@Shadow
	public abstract void teleport(double pX, double pY, double pZ, float pYaw, float pPitch);
	
	@Shadow @Nullable private Vec3 awaitingPositionFromClient;
	
	@Shadow private int awaitingTeleport;
	
	@Inject(at = @At("HEAD"), method = "handleMovePlayer", cancellable = true)
	public void preMove(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
		TeleportationHandler.handlePacket(player, packet);
		if (((IMaySkipPacket) player).skip()) {
//			doSkip = true;
			lastGoodX = player.position().x;
			lastGoodY = player.position().y;
			lastGoodZ = player.position().z;
			boolean noPhys = player.noPhysics;
			player.noPhysics = true;
			player.move(MoverType.SELF, new Vec3(lastGoodX, lastGoodY, lastGoodZ));
			this.player.setOnGround(packet.isOnGround());
			player.absMoveTo(lastGoodX, lastGoodY, lastGoodZ);
			player.noPhysics = noPhys;
			teleport(lastGoodX, lastGoodY, lastGoodZ, packet.getXRot(0), packet.getYRot(0));
//			doSkip = false;
			++this.receivedMovePacketCount;
			int i = this.receivedMovePacketCount - this.knownMovePacketCount;
			if (i > 5) {
				LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), i);
				i = 1;
			}
			ci.cancel();
		}
	}
}
