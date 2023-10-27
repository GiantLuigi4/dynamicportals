package tfc.dynamicportals.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
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
    public void draw(Tesselator tesselator, Minecraft mc, MultiBufferSource.BufferSource source, PoseStack pPoseStack, Matrix4f pProjectionMatrix, Frustum frustum, Camera pCamera, AbstractPortal portal, GameRenderer pGameRenderer, float pPartialTick) {
        if (frustum.isVisible(portal.getContainingBox())) {
            pPoseStack.pushPose();

            Minecraft.getInstance().getMainRenderTarget().enableStencil();

            RenderType.waterMask().setupRenderState();
            RenderSystem.enableCull();

            RenderSystem.disableBlend();
            GL40.glEnable(GL40.GL_DEPTH_CLAMP);
            GL11.glEnable(GL11.GL_STENCIL_TEST);

            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
            GL11.glStencilFunc(GL11.GL_ALWAYS, layer, 0xFF); // all fragments should pass the stencil test
            GL11.glStencilMask(0xFF); // enable writing to the stencil buffer
            // draw stencil
            GameRenderer.getRendertypeWaterMaskShader().apply();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.depthMask(false);
            drawStencil(pPoseStack, pCamera, portal, tesselator);
            RenderSystem.colorMask(true, true, true, true);
            GameRenderer.getRendertypeWaterMaskShader().clear();

            GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0xFF);
            GL11.glStencilMask(0x00);
            RenderSystem.depthMask(true);

            // draw world
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
                RenderSystem.depthFunc(GL11.GL_ALWAYS);
                RenderSystem.setShader(DypoShaders::getDepthClear);
                float[] fog = RenderSystem.getShaderFogColor();
                RenderSystem.setShaderColor(
                        fog[0], fog[1], fog[2], fog[3]
                );

                DypoShaders.getDepthClear().apply();
                drawStencil(pPoseStack, pCamera, portal, tesselator);
                DypoShaders.getDepthClear().clear();
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                RenderType.waterMask().clearRenderState();

                GL11.glDisable(GL40.GL_DEPTH_CLAMP);

                // draw world
                RenderSystem.setShader(GameRenderer::getPositionShader);
                Matrix4f pCpy = pProjectionMatrix.copy();
                RenderSystem.setProjectionMatrix(pCpy);
                dst.renderSky(
                        pPoseStack,
                        pCpy, pPartialTick,
                        pCamera, false, () -> {
                            float f = pGameRenderer.getRenderDistance();
                            boolean flag2 = mc.level.effects().isFoggyAt(Mth.floor(pCamera.getPosition().x), Mth.floor(pCamera.getPosition().z)) || mc.gui.getBossOverlay().shouldCreateWorldFog();
                            FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_SKY, f, flag2, pPartialTick);
                        }
                );
                dst.renderClouds(
                        pPoseStack, pCpy,
                        pPartialTick, pCamera.getPosition().x, pCamera.getPosition().y, pCamera.getPosition().z
                );

                ((CameraAccessor) mc.getBlockEntityRenderDispatcher().camera).setLevel(mcLvl);
                RenderSystem.setProjectionMatrix(pProjectionMatrix);
                mc.level = mcLvl;

                RenderType.waterMask().setupRenderState();
                RenderSystem.enableCull();
            }

            GL11.glEnable(GL40.GL_DEPTH_CLAMP);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);

            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_DECR);
            GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0x00); // all fragments should pass the stencil test
            GL11.glStencilMask(0xFF); // enable writing to the stencil buffer
            // draw stencil
            GameRenderer.getRendertypeWaterMaskShader().apply();
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.depthMask(true);
            drawStencil(pPoseStack, pCamera, portal, tesselator);
            RenderSystem.colorMask(true, true, true, true);
            GameRenderer.getRendertypeWaterMaskShader().clear();

            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);

            GL11.glDisable(GL11.GL_STENCIL_TEST);
            GL11.glDisable(GL40.GL_DEPTH_CLAMP);

            pPoseStack.popPose();

            RenderType.waterMask().clearRenderState();
            RenderSystem.enableCull();
        }

        float f = pGameRenderer.getRenderDistance();
        boolean flag2 = mc.level.effects().isFoggyAt(Mth.floor(pCamera.getPosition().x), Mth.floor(pCamera.getPosition().z)) || mc.gui.getBossOverlay().shouldCreateWorldFog();
        FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_TERRAIN, f, flag2, pPartialTick);
    }

    void drawStencil(PoseStack pPoseStack, Camera pCamera, AbstractPortal portal, Tesselator tesselator) {
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.vertex(
                pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
        ).color(255, 0, 0, 255).endVertex();
        builder.vertex(
                pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
        ).color(255, 0, 0, 255).endVertex();
        builder.vertex(
                pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
        ).color(255, 0, 0, 255).endVertex();
        builder.vertex(
                pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
        ).color(255, 0, 0, 255).endVertex();
        tesselator.end();
    }
}
