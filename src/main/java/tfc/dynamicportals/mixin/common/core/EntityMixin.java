package tfc.dynamicportals.mixin.common.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.TeleportationHandler;
import tfc.dynamicportals.util.TeleportationData;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public abstract void setDeltaMovement(Vec3 pMotion);
	
	//eh, basically you have to modify the pPos argument
	@Inject(method = "move", at = @At("HEAD"))
	public void teleport(MoverType pType, Vec3 pPos, CallbackInfo ci) {
		TeleportationData data = TeleportationHandler.getTeleportationData((Entity) (Object) this, pPos);
//		if (data != null) {
//			teleportationTick = true;
//			teleportationData = data;
//		}
		setDeltaMovement(data == null ? pPos : data.motion);
	}
}
