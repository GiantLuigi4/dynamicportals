package tfc.dynamicportals.mixin.client;

import net.minecraft.Util;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Util.class)
public class MojangWhy {
	@Inject(at = @At("TAIL"), method = "getMaxThreads", cancellable = true)
	private static void preGetThreads(CallbackInfoReturnable<Integer> cir) {
		// 255 is too much as a default
		if (!FMLEnvironment.production)
			cir.setReturnValue(3);
	}
}
