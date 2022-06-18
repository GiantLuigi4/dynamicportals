package tfc.dynamicportals.api;

import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.lwjgl.opengl.GL11;

public class Portal extends AbstractPortal {
	Vector3d position;
	Vector2d size;
	Vector2d rotation;
	Vector3f normal;
	
	public void computeNormal() {
		Vector3f portalPos = new Vector3f((float) position.x, (float) position.y, (float) position.z);
//		Vector3f a = portalPos.copy();
//		a.add((float) -portal.size.x / 2, (float) portal.size.y, 0);
		Vector3f b = portalPos.copy();
		b.add((float) size.x / 2, (float) size.y, 0);
		Vector3f c = portalPos.copy();
		c.add((float) -size.x / 2, 0, 0);
		Vector3f d = portalPos.copy();
		d.add((float) size.x / 2, 0, 0);

		Matrix3f matrix3f = new Matrix3f();
		matrix3f.setIdentity();
		matrix3f.mul(new Quaternion((float) rotation.y, (float) rotation.x, 0, false));
//		a.transform(matrix3f);
		b.transform(matrix3f);
		c.transform(matrix3f);
		d.transform(matrix3f);

		Vector3f first = b.copy();
		first.sub(d);
		Vector3f second = c.copy();
		second.sub(d);

		first.cross(second);
	}
	
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
		// translate
		stack.translate(position.x, position.y, position.z);
		// rotate
		stack.mulPose(new Quaternion((float) rotation.y, (float) rotation.x, 0, false));
	}
	
	@Override
	public void negateTransform(PoseStack stack) {
		// translate
		stack.translate(position.x, position.y, position.z);
		// rotate
		// TODO: figure out vertical rotation
		stack.mulPose(new Quaternion(0, (float) rotation.x, 0, false));
//		stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
	}
	
	@Override
	public void setupAsTarget(PoseStack stack) {
		// translate
		stack.translate(-position.x, -position.y, -position.z);
		// rotate
		// TODO: figure out vertical rotation
		stack.mulPose(new Quaternion(0, (float) rotation.x, 0, false));
//		stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
		// mirror
		stack.mulPose(new Quaternion(0, 0, 0, true));
//		stack.mulPose(new Quaternion(180, 0, 0, true));
		stack.scale(1, 1, -1);
	}
	
	@Override
	public boolean shouldRender(Frustum frustum, double camX, double camY, double camZ) {
		if (normal == null || normal.dot(new Vector3f((float) (camX - position.x), (float) (camY - position.y), (float) (camZ - position.z))) > 0) {
			// TODO: deal with rotation
//			AABB box = new AABB(
//					position.x - size.x / 2, position.y, position.z - size.x / 2,
//					position.x + size.x / 2, position.y + size.y, position.z + size.x / 2
//			);
//			return frustum.isVisible(box);
			return true;
		}
		return false;
	}
	
	@Override
	public void setupRenderState() {
		// TODO: check if this works well enough
		GL11.glCullFace(GL11.GL_FRONT);
	}
	
	@Override
	public void teardownRenderState() {
		GL11.glCullFace(GL11.GL_BACK);
	}
}
