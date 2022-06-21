package tfc.dynamicportals.mixin.common.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.access.IMaySkipPacket;
import tfc.dynamicportals.api.AbstractPortal;

@Mixin(Entity.class)
public abstract class EntityMixin implements IMaySkipPacket {
	@Shadow
	public Level level;
	
	@Shadow
	public abstract Vec3 getPosition(float pPartialTicks);
	
	@Shadow
	private Vec3 deltaMovement;
	@Unique
	private boolean skipTeleportPacket = false;
	@Unique
	private boolean skipMotSet = false;
	
	@Inject(at = @At("HEAD"), method = "collide")
	public void preMove(Vec3 vec31, CallbackInfoReturnable<Vec3> cir) {
		AbstractPortal[] portals = Temp.getPortals(level);
		for (AbstractPortal portal : portals) {
			if (portal.isInfront((Entity) (Object) this, this.getPosition(0))) {
				if (portal.moveEntity((Entity) (Object) this, this.getPosition(0), vec31)) {
					skipTeleportPacket = true;
					skipMotSet = true;
					// TODO: better handling, deny teleporting through the pair
					break;
				}
			}
		}
	}
	
	@Override
	public boolean skip() {
		boolean old = skipTeleportPacket;
		skipTeleportPacket = false;
		return old;
	}
}
