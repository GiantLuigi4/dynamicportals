package tfc.dynamicportals.mixin.client.render.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.client.render.debug.FrustumDrawer;
import tfc.dynamicportals.client.render.debug.NetworkDrawer;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class MixinDebugRendering {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Nullable
    private ClientLevel level;
    @Shadow
    private boolean captureFrustum;

    @Shadow
    @Nullable
    public Frustum capturedFrustum;

    @Shadow
    private Frustum cullingFrustum;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0), method = "renderLevel")
    public void debugDraw(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        NetworkDrawer.debugDraw(minecraft, level, pPoseStack, pPartialTick, pFinishNanoTime, pRenderBlockOutline, pCamera, pGameRenderer, pLightTexture, pProjectionMatrix, ci);
        FrustumDrawer.draw(
                pPoseStack, pPartialTick, pFinishNanoTime,
                pRenderBlockOutline, pCamera,
                pGameRenderer,
                pLightTexture, pProjectionMatrix,
                captureFrustum ? capturedFrustum : cullingFrustum
        );
    }
}
