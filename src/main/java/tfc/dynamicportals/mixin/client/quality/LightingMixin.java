package tfc.dynamicportals.mixin.client.quality;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tfc.dynamicportals.util.lighting.LightingNonsense;

@Mixin(Lighting.class)
public class LightingMixin {
	// TODO: I'd like this to not be a mixin
	@ModifyVariable(at = @At("HEAD"), method = "setupLevel", index = 0, argsOnly = true)
	private static Matrix4f modifyMatr0(Matrix4f matr) {
		return LightingNonsense.modifyMatrix(matr);
	}
	
	// TODO: I'd like this to not be a mixin
	@ModifyVariable(at = @At("HEAD"), method = "setupNetherLevel", index = 0, argsOnly = true)
	private static Matrix4f modifyMatr1(Matrix4f matr) {
		return LightingNonsense.modifyMatrix(matr);
	}
}
