package tfc.dynamicportals.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractPortal {
	public AbstractPortal target = this;
	
	public void setTarget(Portal target) {
		this.target = target;
	}
	
	public abstract void drawStencil(VertexConsumer builder, PoseStack stack);
	public abstract void setupMatrix(PoseStack stack);
	public abstract void setupAsTarget(PoseStack stack);
	public abstract void negateTransform(PoseStack stack);
	public abstract void negateTrace(PoseStack stack);
	public abstract void setupTrace(PoseStack stack);
	public abstract boolean shouldRender(Frustum frustum, double camX, double camY, double camZ);
	
	public abstract double trace(Vec3 start, Vec3 end);
	
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
	}
	
	public void setupRenderState() {
	}
	
	public void teardownRenderState() {
	}
	
}
