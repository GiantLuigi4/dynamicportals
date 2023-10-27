package tfc.dynamicportals.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.mixin.client.data.access.CameraAccessor;
import tfc.dynamicportals.mixin.client.data.access.LevelRendererAccessor;
import tfc.dynamicportals.util.DypoShaders;

public class FastRenderer extends AbstractPortalRenderDispatcher {
    int layer = 0;

    @Override
    public void push(int layer) {
        this.layer = layer;
    }

    @Override
    public void pop(int layer) {
        this.layer = layer;
    }

    @Override
    public void draw(Tesselator tesselator, Minecraft mc, MultiBufferSource.BufferSource source, PoseStack pPoseStack, Matrix4f pProjectionMatrix, Frustum frustum, Camera pCamera, AbstractPortal portal, GameRenderer pGameRenderer, float pPartialTick, LightTexture lightTexture, boolean renderOutline, long finish) {
        if (frustum.isVisible(portal.getContainingBox())) {
            pPoseStack.pushPose();

            Minecraft.getInstance().getMainRenderTarget().enableStencil();

            RenderType.waterMask().setupRenderState();
            RenderSystem.enableCull();

            RenderSystem.disableBlend();
            GL40.glEnable(GL40.GL_DEPTH_CLAMP);

            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
            GL11.glStencilFunc(GL11.GL_EQUAL, layer, 0xFF); // all fragments should pass the stencil test
            GL11.glStencilMask(0xFF); // enable writing to the stencil buffer
            // draw stencil
            GameRenderer.getRendertypeWaterMaskShader().apply();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.depthMask(false);
            drawStencil(pPoseStack, pCamera.getPosition(), portal, tesselator);
            RenderSystem.colorMask(true, true, true, true);
            GameRenderer.getRendertypeWaterMaskShader().clear();

            GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0xFF);
            GL11.glStencilMask(0x00);
            RenderSystem.depthMask(true);

            // draw world
            drawing = portal;
            LevelRenderer dst = portal.getTargetLevelRenderer();
            if (dst != null) {
                ClientLevel mcLvl = mc.level;
                mc.level = ((LevelRendererAccessor) dst).getLevel();
                ((CameraAccessor) mc.getBlockEntityRenderDispatcher().camera).setLevel(mc.level);

                {
                    float f = pGameRenderer.getRenderDistance();
                    if (mc.level.dimension().location().toString().equals("minecraft:the_nether") && !mc.level.isLoaded(pCamera.getBlockPosition())) {
                        RenderSystem.setShaderFogColor(0.16265765f, 0.025490593f, 0.025490593f, 1.0f);
                    } else {
                        boolean flag2 = mc.level.effects().isFoggyAt(Mth.floor(pCamera.getPosition().x), Mth.floor(pCamera.getPosition().z)) || mc.gui.getBossOverlay().shouldCreateWorldFog();
                        FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_SKY, f, flag2, pPartialTick);
                        FogRenderer.levelFogColor();
                    }
                }

                // clear
                // TODO: remove in favor of a glClear override
                RenderSystem.setShader(DypoShaders::getDepthClear);
                float[] fog = RenderSystem.getShaderFogColor();
                RenderSystem.setShaderColor(
                        fog[0], fog[1], fog[2], fog[3]
                );

                RenderSystem.depthFunc(GL11.GL_ALWAYS);
                DypoShaders.getDepthClear().apply();
                drawStencil(pPoseStack, pCamera.getPosition(), portal, tesselator);
                DypoShaders.getDepthClear().clear();
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                RenderType.waterMask().clearRenderState();

                GL11.glDisable(GL40.GL_DEPTH_CLAMP);

                // draw world
                RenderSystem.setShader(GameRenderer::getPositionShader);
                Matrix4f pCpy = pProjectionMatrix.copy();
                RenderSystem.setProjectionMatrix(pCpy);
                PoseStack rsStack = RenderSystem.getModelViewStack();
                RenderSystem.modelViewStack = new PoseStack();
                PoseStack poseCopy = new PoseStack();
                poseCopy.last().pose().load(pPoseStack.last().pose());
                poseCopy.last().normal().load(pPoseStack.last().normal());
                poseCopy.translate(-2, 0, 0);
                dst.prepareCullFrustum(
                        poseCopy,
                        pCamera.getPosition(),
                        pProjectionMatrix
                );
                dst.renderLevel(
                        poseCopy, pPartialTick,
                        finish, renderOutline,
                        pCamera, pGameRenderer,
                        lightTexture, pCpy
                );
                drawing = portal;
                RenderSystem.modelViewStack = rsStack;

                ((CameraAccessor) mc.getBlockEntityRenderDispatcher().camera).setLevel(mcLvl);
                RenderSystem.setProjectionMatrix(pProjectionMatrix);
                mc.level = mcLvl;

                RenderType.waterMask().setupRenderState();
                RenderSystem.enableCull();
            }
            pPoseStack.popPose();

            GL11.glEnable(GL40.GL_DEPTH_CLAMP);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);

            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_DECR);
            GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0xFF);
            GL11.glStencilMask(0xFF);
            // draw stencil
            GameRenderer.getRendertypeWaterMaskShader().apply();
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(true);
            drawStencil(pPoseStack, pCamera.getPosition(), portal, tesselator);
            RenderSystem.colorMask(true, true, true, true);
            GameRenderer.getRendertypeWaterMaskShader().clear();

            // disable stencil writing
            GL11.glStencilMask(0x00);
            GL11.glStencilFunc(GL11.GL_EQUAL, layer, 0xFF);

            GL11.glDisable(GL40.GL_DEPTH_CLAMP);


            RenderType.waterMask().clearRenderState();
            RenderSystem.enableCull();
        }

        drawing = null;
    }
}
