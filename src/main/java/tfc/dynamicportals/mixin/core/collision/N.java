package tfc.dynamicportals.mixin.core.collision;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class N {
	@Shadow
	public ServerPlayer player;
	
	@Shadow
	private boolean clientIsFloating;
	
	@Shadow
	private double lastGoodX;
	
	@Shadow
	private double lastGoodY;
	
	@Shadow
	private double lastGoodZ;
	
	@Shadow
	private int awaitingTeleportTime;
	
	@Shadow
	private int tickCount;
	
	@Shadow
	private static double clampHorizontal(double pValue) {
		return 0;
	}
	
	@Shadow
	private static double clampVertical(double pValue) {
		return 0;
	}
	
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void handleMovePlayer(ServerboundMovePlayerPacket pPacket) {
		this.awaitingTeleportTime = this.tickCount;
		
		double d0 = clampHorizontal(pPacket.getX(this.player.getX()));
		double d1 = clampVertical(pPacket.getY(this.player.getY()));
		double d2 = clampHorizontal(pPacket.getZ(this.player.getZ()));
		float f = Mth.wrapDegrees(pPacket.getYRot(this.player.getYRot()));
		float f1 = Mth.wrapDegrees(pPacket.getXRot(this.player.getXRot()));
		
		double d3 = this.player.getX();
		double d4 = this.player.getY();
		double d5 = this.player.getZ();
		
		this.player.absMoveTo(d0, d1, d2, f, f1);
		this.player.serverLevel().getChunkSource().move(this.player);
		this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, pPacket.isOnGround());
		this.player.setOnGroundWithKnownMovement(pPacket.isOnGround(), new Vec3(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5));
		if (true) {
			this.player.resetFallDistance();
		}
		
		this.player.m_36378_(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
		this.lastGoodX = this.player.getX();
		this.lastGoodY = this.player.getY();
		this.lastGoodZ = this.player.getZ();
	}
}
