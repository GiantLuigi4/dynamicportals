package tfc.dynamicportals.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;

public abstract class AbstractPortal {
	public abstract void drawStencil(VertexConsumer builder, PoseStack stack);
	public abstract void setupMatrix(PoseStack stack);
	public abstract void setupAsTarget(PoseStack stack);
	public abstract void negateTransform(PoseStack stack);
	public abstract boolean shouldRender(Frustum frustum, double camX, double camY, double camZ);
	
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
	}
	
	public void setupRenderState() {
	}
	
	public void teardownRenderState() {
	}
}
