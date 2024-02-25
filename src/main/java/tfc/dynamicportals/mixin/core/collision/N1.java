package tfc.dynamicportals.mixin.core.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LivingEntity.class)
public class N1 {
	@Overwrite
	public boolean isInWall() {
		return false;
	}
}
