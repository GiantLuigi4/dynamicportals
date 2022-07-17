package tfc.dynamicportals.mixin.common.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tfc.dynamicportals.TeleportationHandler;
import tfc.dynamicportals.access.ITeleportTroughPacket;

@Mixin(Entity.class)
public abstract class EntityMixin implements ITeleportTroughPacket {
	@Shadow
	public Level level;
	@Shadow
	private Vec3 deltaMovement;
	@Unique
	private boolean teleported = false;
	
	@Shadow
	public abstract Vec3 getPosition(float pPartialTicks);
	
	@ModifyVariable(at = @At("HEAD"), method = "move", index = 2, argsOnly = true)
	public Vec3 preMove(Vec3 motion) {
		Vec3 vec = TeleportationHandler.getTeleportedMotion((Entity) (Object) this, motion);
		return vec == null ? motion : vec;
	}
	
	@Override
	public void setTeleported() {
		teleported = true;
	}
	
	@Override
	public boolean hasTeleported() {
		boolean old = teleported;
		teleported = false;
		return old;
	}
}
