package tfc.dynamicportals.mixin.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.client.AbstractPortalRenderDispatcher;
import tfc.dynamicportals.util.ClearFunc;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
    @Inject(at = @At("HEAD"), method = "_clear", cancellable = true)
    private static void preClear(int pMask, boolean pCheckError, CallbackInfo ci) {
        if (AbstractPortalRenderDispatcher.getDrawing() == null) return;
        ClearFunc.clear(pMask);
        ci.cancel();
    }
}
