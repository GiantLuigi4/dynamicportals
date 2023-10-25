package tfc.dynamicportals.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import tfc.dynamicportals.api.AbstractPortal;

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
            Minecraft.getInstance().getMainRenderTarget().enableStencil();

            VertexConsumer consumer = source.getBuffer(RenderType.LINES);
            consumer.vertex(
                            pPoseStack.last().pose(),
                            (float) (portal.getPosition().x - pCamera.getPosition().x),
                            (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                            (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
                    ).color(255, 255, 255, 255)
                    .normal(pPoseStack.last().normal(), 0, 0, 1)
                    .endVertex();
            consumer.vertex(
                            pPoseStack.last().pose(),
                            (float) (portal.getPosition().x - pCamera.getPosition().x),
                            (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                            (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
                    ).color(255, 255, 255, 255)
                    .normal(pPoseStack.last().normal(), 0, 0, 1)
                    .endVertex();

            consumer.vertex(
                            pPoseStack.last().pose(),
                            (float) (portal.getPosition().x - pCamera.getPosition().x),
                            (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                            (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
                    ).color(255, 255, 255, 255)
                    .normal(pPoseStack.last().normal(), 0, 0, 1)
                    .endVertex();
            consumer.vertex(
                            pPoseStack.last().pose(),
                            (float) (portal.getPosition().x - pCamera.getPosition().x),
                            (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                            (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
                    ).color(255, 255, 255, 255)
                    .normal(pPoseStack.last().normal(), 0, 0, 1)
                    .endVertex();

            consumer.vertex(
                            pPoseStack.last().pose(),
                            (float) (portal.getPosition().x - pCamera.getPosition().x),
                            (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                            (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
                    ).color(255, 255, 255, 255)
                    .normal(pPoseStack.last().normal(), 0, 1, 0)
                    .endVertex();
            consumer.vertex(
                            pPoseStack.last().pose(),
                            (float) (portal.getPosition().x - pCamera.getPosition().x),
                            (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                            (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
                    ).color(255, 255, 255, 255)
                    .normal(pPoseStack.last().normal(), 0, 1, 0)
                    .endVertex();

            consumer.vertex(
                            pPoseStack.last().pose(),
                            (float) (portal.getPosition().x - pCamera.getPosition().x),
                            (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                            (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
                    ).color(255, 255, 255, 255)
                    .normal(pPoseStack.last().normal(), 0, 1, 0)
                    .endVertex();
            consumer.vertex(
                            pPoseStack.last().pose(),
                            (float) (portal.getPosition().x - pCamera.getPosition().x),
                            (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                            (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
                    ).color(255, 255, 255, 255)
                    .normal(pPoseStack.last().normal(), 0, 1, 0)
                    .endVertex();
            source.endBatch();

            GL40.glEnable(GL40.GL_DEPTH_CLAMP);

            GL11.glEnable(GL11.GL_STENCIL_TEST);

            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
            GL11.glStencilFunc(GL11.GL_ALWAYS, layer, 0xFF); // all fragments should pass the stencil test
            GL11.glStencilMask(0xFF); // enable writing to the stencil buffer
            // draw stencil
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GameRenderer.getRendertypeWaterMaskShader().apply();
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);
            BufferBuilder builder = tesselator.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            builder.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                    (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
            ).endVertex();
            builder.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                    (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
            ).endVertex();
            builder.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                    (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
            ).endVertex();
            builder.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                    (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
            ).endVertex();
            tesselator.end();
            GameRenderer.getRendertypeWaterMaskShader().clear();

            GL11.glColorMask(true, true, true, true);
            GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0xFF);
            GL11.glStencilMask(0x00);
            GL11.glDepthMask(true);
            // draw world
            consumer = source.getBuffer(RenderType.leash());
            consumer.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x - 1),
                    (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
            ).color(255, 0, 0, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
            consumer.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x - 1),
                    (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
            ).color(255, 0, 0, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
            consumer.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x - 2),
                    (float) (portal.getPosition().y - pCamera.getPosition().y + 2),
                    (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
            ).color(255, 0, 0, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
            source.endBatch();

            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_DECR);
            GL11.glStencilFunc(GL11.GL_EQUAL, layer+1, 0x00); // all fragments should pass the stencil test
            GL11.glStencilMask(0xFF); // enable writing to the stencil buffer
            // draw stencil
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GameRenderer.getRendertypeWaterMaskShader().apply();
            GL11.glColorMask(false, false, false, false);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            builder.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                    (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
            ).endVertex();
            builder.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                    (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z - 1)
            ).endVertex();
            builder.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                    (float) (portal.getPosition().y - pCamera.getPosition().y + 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
            ).endVertex();
            builder.vertex(
                    pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
                    (float) (portal.getPosition().y - pCamera.getPosition().y - 1),
                    (float) (portal.getPosition().z - pCamera.getPosition().z + 1)
            ).endVertex();
            tesselator.end();
            GameRenderer.getRendertypeWaterMaskShader().clear();

            GL11.glColorMask(true, true, true, true);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);

            GL11.glStencilMask(0xFF);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
            GL11.glStencilMask(0x00);

            GL11.glDisable(GL11.GL_STENCIL_TEST);
            GL11.glDisable(GL40.GL_DEPTH_CLAMP);
        }
    }
}
