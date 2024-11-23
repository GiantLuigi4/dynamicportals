package tfc.dynamicportals.mixin.client.render.optim.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class DisableThreadAssertations {
    @Inject(at = @At("HEAD"), method = "assertOnRenderThread", cancellable = true)
    private static void preOnRenderThread(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "assertOnGameThread", cancellable = true)
    private static void preOnGameThread(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "assertOnGameThreadOrInit", cancellable = true)
    private static void preOnGameOrInitThread(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "assertOnRenderThreadOrInit", cancellable = true)
    private static void preOnRenderOrInitThread(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "assertInInitPhase", cancellable = true)
    private static void preIsInit(CallbackInfo ci) {
        ci.cancel();
    }
}
