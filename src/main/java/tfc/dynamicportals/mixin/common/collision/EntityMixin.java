package tfc.dynamicportals.mixin.common.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.TeleportationHandler;
import tfc.dynamicportals.access.IMaySkipPacket;

@Mixin(Entity.class)
public abstract class EntityMixin implements IMaySkipPacket {
	@Shadow
	public Level level;
	@Shadow
	private Vec3 deltaMovement;
	@Unique
	private boolean skipTeleportPacket = false;

	@Shadow
	public abstract Vec3 getPosition(float pPartialTicks);

	@ModifyVariable(at = @At("HEAD"), method = "move", index = 2, argsOnly = true)
	public Vec3 preMove(Vec3 motion) {
		Vec3 vec = TeleportationHandler.handle((Entity) (Object) this, motion);
		if (vec != null) return vec;
		else return motion;
	}

	@Override
	public void setSkipTeleportPacket() {
		skipTeleportPacket = true;
	}

	@Override
	public boolean skip() {
		boolean old = skipTeleportPacket;
		skipTeleportPacket = false;
		return old;
	}
}
