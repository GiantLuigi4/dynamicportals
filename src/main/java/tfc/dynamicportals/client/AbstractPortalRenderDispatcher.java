package tfc.dynamicportals.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.AbstractPortal;

/**
 * A base class for portal renderers
 * I plan to have multiple different renderers in the future, so this is here for when and if that happens
 * <p>
 * Particularly, I plan for
 * - geometry shader based one
 * - stencil based one
 */
public abstract class AbstractPortalRenderDispatcher {
    private static AbstractPortalRenderDispatcher SELECTED = new FastRenderer();

    public static AbstractPortalRenderDispatcher getSelected() {
        return SELECTED;
    }

    protected static AbstractPortal drawing;

    public abstract void push(int layer);

    public abstract void pop(int layer);

    public abstract void draw(
            Tesselator tesselator,
            Minecraft mc, MultiBufferSource.BufferSource source,
            PoseStack pPoseStack, Matrix4f pProjectionMatrix,
            Frustum frustum, Camera pCamera,
            AbstractPortal portal,
            GameRenderer pGameRenderer,
            float pPartialTick,
            LightTexture lightTexture,
            boolean renderOutline, long finish
    );

    public static AbstractPortal getDrawing() {
        return drawing;
    }

    public static void drawStencil(PoseStack pPoseStack, Vec3 pCamera, AbstractPortal portal, Tesselator tesselator) {
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.vertex(
                pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.x),
                (float) (portal.getPosition().y - pCamera.y - 1),
                (float) (portal.getPosition().z - pCamera.z - 1)
        ).endVertex();
        builder.vertex(
                pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.x),
                (float) (portal.getPosition().y - pCamera.y + 1),
                (float) (portal.getPosition().z - pCamera.z - 1)
        ).endVertex();
        builder.vertex(
                pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.x),
                (float) (portal.getPosition().y - pCamera.y + 1),
                (float) (portal.getPosition().z - pCamera.z + 1)
        ).endVertex();
        builder.vertex(
                pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.x),
                (float) (portal.getPosition().y - pCamera.y - 1),
                (float) (portal.getPosition().z - pCamera.z + 1)
        ).endVertex();
        tesselator.end();
    }
}
