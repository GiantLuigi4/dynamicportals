package tfc.dynamicportals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import tfc.dynamicportals.api.AbstractPortal;

/**
 * A base class for portal renderers
 * I plan to have multiple different renderers in the future, so this is here for when and if that happens
 *
 * Particularly, I plan for
 * - geometry shader based one
 * - stencil based one
 */
public abstract class AbstractPortalRenderDispatcher {
    private static AbstractPortalRenderDispatcher SELECTED = new FastRenderer();

    public static AbstractPortalRenderDispatcher getSelected() {
        return SELECTED;
    }

    public abstract void push(int layer);

    public abstract void pop(int layer);

    public abstract void draw(
            Tesselator tesselator,
            Minecraft mc, MultiBufferSource.BufferSource source,
            PoseStack pPoseStack, Matrix4f pProjectionMatrix,
            Frustum frustum, Camera pCamera,
            AbstractPortal portal,
            GameRenderer pGameRenderer,
            float pPartialTick);
	
	public abstract boolean supportsRecurse();
}
