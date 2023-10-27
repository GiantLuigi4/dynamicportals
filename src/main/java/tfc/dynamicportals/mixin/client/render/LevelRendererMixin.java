package tfc.dynamicportals.mixin.client.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;
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
    private static int recurse = 0;

    @Inject(at = @At("HEAD"), method = "renderLevel")
    public void preDrawLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        if (recurse < 0) recurse = 0;
        recurse++;
    }

    @Inject(at = @At("RETURN"), method = "renderLevel")
    public void postDrawLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        recurse--;
        if (recurse < 0) recurse = 0;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V", ordinal = 3), method = "renderLevel")
    public void drawPortals(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        if (recurse <= 2) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);

            AbstractPortalRenderDispatcher renderer = AbstractPortalRenderDispatcher.getSelected();
            renderer.push(recurse - 1);
            Tesselator tessel = Tesselator.getInstance();
            for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
                for (AbstractPortal portal : portalNetwork.getPortals()) {
                    if (portal.myLevel == level) {
                        if (portal.preferredDispatcher() != null)
                            portal.preferredDispatcher().draw(tessel, minecraft, minecraft.renderBuffers().bufferSource(), pPoseStack, pProjectionMatrix, captureFrustum ? capturedFrustum : cullingFrustum, pCamera, portal, pGameRenderer, pPartialTick, pLightTexture, pRenderBlockOutline, pFinishNanoTime);
                        else
                            renderer.draw(tessel, minecraft, minecraft.renderBuffers().bufferSource(), pPoseStack, pProjectionMatrix, captureFrustum ? capturedFrustum : cullingFrustum, pCamera, portal, pGameRenderer, pPartialTick, pLightTexture, pRenderBlockOutline, pFinishNanoTime);
                    }
                }
            }
            renderer.pop(recurse - 1);
            minecraft.renderBuffers().bufferSource().endBatch();

            // disable stencil
            if (recurse == 1) {
                GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
                GL11.glStencilMask(0xFF);
                GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
                GL11.glStencilMask(0x00);
                GL11.glDisable(GL11.GL_STENCIL_TEST);
            }
        }

        // correct fog
        float f = pGameRenderer.getRenderDistance();
        boolean flag2 = minecraft.level.effects().isFoggyAt(Mth.floor(pCamera.getPosition().x), Mth.floor(pCamera.getPosition().z)) || minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_TERRAIN, f, flag2, pPartialTick);

        // correct lighting
        {
            PoseStack rsStack = RenderSystem.getModelViewStack();
            rsStack.pushPose();
            rsStack.setIdentity();

            net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(pGameRenderer, pCamera, pPartialTick);
            pCamera.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
            rsStack.mulPose(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));

            rsStack.mulPose(Vector3f.XP.rotationDegrees(pCamera.getXRot()));
            rsStack.mulPose(Vector3f.YP.rotationDegrees(pCamera.getYRot() + 180.0F));

            RenderSystem.applyModelViewMatrix();
            Lighting.setupLevel(rsStack.last().pose());
            rsStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }
}
