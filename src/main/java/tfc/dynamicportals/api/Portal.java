package tfc.dynamicportals.api;

import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import org.lwjgl.opengl.GL11;
import tfc.dynamicportals.GLUtils;

public class Portal extends AbstractPortal {
	Vector3d position;
	Vector2d size;
	Vector2d rotation;
	
	@Override
	public void drawStencil(VertexConsumer builder, PoseStack stack) {
		float r = 1, b = r, g = b, a = g;
		Matrix4f mat = stack.last().pose();
		// TODO: use a custom vertex builder which automatically fills in missing elements
		builder.vertex(mat, -((float) size.x / 2), 0, 0).color(r, g, b, a).uv(0, 0).endVertex();
		builder.vertex(mat, ((float) size.x / 2), 0, 0).color(r, g, b, a).uv(0, 0).endVertex();
		builder.vertex(mat, ((float) size.x / 2), (float) size.y, 0).color(r, g, b, a).uv(0, 0).endVertex();
		builder.vertex(mat, -((float) size.x / 2), (float) size.y, 0).color(r, g, b, a).uv(0, 0).endVertex();
	}
	
	@Override
	public void setupMatrix(PoseStack stack) {
		if (position != null) stack.translate(position.x, position.y, position.z);
		if (rotation != null) stack.mulPose(new Quaternion((float) rotation.y, (float) rotation.x, 0, false));
	}
	
	@Override
	public void negateTransform(PoseStack stack) {
		if (position != null) stack.translate(position.x, position.y, position.z);
		if (rotation != null) stack.mulPose(new Quaternion((float) -rotation.y, (float) -rotation.x, 0, false));
	}
	
	@Override
	public void setupAsTarget(PoseStack stack) {
//		stack.scale(1, 1, -1);
		if (rotation != null) stack.mulPose(new Quaternion((float) rotation.y, (float) rotation.x, 0, false));
		if (position != null) stack.translate(-position.x, position.y, -position.z);
		stack.mulPose(new Quaternion(180, 90, 0, true));
	}
	
	@Override
	public void setupRenderState() {
		// TODO: check if this works well enough
//		GL11.glCullFace(GL11.GL_FRONT);
	}
	
	@Override
	public void teardownRenderState() {
//		GL11.glCullFace(GL11.GL_BACK);
	}
}
