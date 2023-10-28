package tfc.dynamicportals.mixin.client.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.mixin.client.data.access.FrustumAccessor;
import tfc.dynamicportals.util.debug.FrustumDrawer;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin2 {
    @Shadow
    private boolean captureFrustum;

    @Shadow
    @Nullable
    public Frustum capturedFrustum;

    @Shadow
    private Frustum cullingFrustum;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0), method = "renderLevel")
    public void debugDraw(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        FrustumDrawer.draw(
                pPoseStack, pPartialTick, pFinishNanoTime,
                pRenderBlockOutline, pCamera,
                pGameRenderer,
                pLightTexture, pProjectionMatrix,
                captureFrustum ? capturedFrustum : cullingFrustum
        );
    }
}
