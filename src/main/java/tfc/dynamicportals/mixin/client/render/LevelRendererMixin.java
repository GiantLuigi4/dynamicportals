package tfc.dynamicportals.mixin.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.client.AbstractPortalRenderDispatcher;
import tfc.dynamicportals.itf.NetworkHolder;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
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
    @Unique
    int recurse = 0;

    @Inject(at = @At("HEAD"), method = "renderLevel")
    public void preDrawLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        recurse++;
    }

    @Inject(at = @At("RETURN"), method = "renderLevel")
    public void postDrawLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        recurse--;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V", ordinal = 3), method = "renderLevel")
    public void drawPortals(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        if (recurse == 1) {
            AbstractPortalRenderDispatcher renderer = AbstractPortalRenderDispatcher.getSelected();
            renderer.push(recurse - 1);
            Tesselator tessel = Tesselator.getInstance();
            for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
                for (AbstractPortal portal : portalNetwork.getPortals()) {
                    if (portal.myLevel == level) {
                        if (portal.preferredDispatcher() != null)
                            portal.preferredDispatcher().draw(tessel, minecraft, minecraft.renderBuffers().bufferSource(), pPoseStack, pProjectionMatrix, captureFrustum ? capturedFrustum : cullingFrustum, pCamera, portal, pGameRenderer, pPartialTick);
                        else
                            renderer.draw(tessel, minecraft, minecraft.renderBuffers().bufferSource(), pPoseStack, pProjectionMatrix, captureFrustum ? capturedFrustum : cullingFrustum, pCamera, portal, pGameRenderer, pPartialTick);
                    }
                }
            }
            renderer.pop(recurse - 1);
            minecraft.renderBuffers().bufferSource().endBatch();
        }
    }
}
