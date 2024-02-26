package tfc.dynamicportals.client.renderer;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.itf.ClientPortalType;

public abstract class AbstractPortalRenderer<T extends AbstractPortal> {
	public AbstractPortalRenderer(PortalType<T> type) {
		((ClientPortalType) type).setRenderer(this);
	}
	
	public abstract void drawStencil(MultiBufferSource.BufferSource source, PoseStack pPoseStack, Camera pCamera, T portal, Tesselator tesselator);
	
	public abstract void drawOverlay(MultiBufferSource.BufferSource source, PoseStack pPoseStack, Camera pCamera, T portal, Tesselator tesselator);
	
	public static void drawQuad(PoseStack pPoseStack, double xSize, double ySize, Tesselator tesselator) {
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		builder.vertex(
				pPoseStack.last().pose(),
				(float) (-xSize),
				(float) (-ySize),
				(float) (0)
		).color(255, 0, 0, 255).endVertex();
		builder.vertex(
				pPoseStack.last().pose(),
				(float) (-xSize),
				(float) (ySize),
				(float) (0)
		).color(255, 0, 0, 255).endVertex();
		builder.vertex(
				pPoseStack.last().pose(),
				(float) (xSize),
				(float) (ySize),
				(float) (0)
		).color(255, 0, 0, 255).endVertex();
		builder.vertex(
				pPoseStack.last().pose(),
				(float) (xSize),
				(float) (-ySize),
				(float) (0)
		).color(255, 0, 0, 255).endVertex();
		tesselator.end();
	}
	
	
	public abstract void setupMatrix(T portal, PoseStack stack);
	
	public abstract void setupAsTarget(T portal, PoseStack stack);
}
