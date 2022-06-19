package tfc.dynamicportals.api;

import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import tfc.dynamicportals.DynamicPortals;

public class Portal extends AbstractPortal {
	Vector3d position;
	Vector2d size;
	Vector2d rotation;
	Vector3f normal;
	
	public Portal setPosition(double x, double y, double z) {
		this.position = new Vector3d(x, y, z);
		return this;
	}
	
	public Portal setPosition(Vector3d position) {
		this.position = position;
		return this;
	}
	
	public Portal setSize(double x, double y) {
		this.size = new Vector2d(x, y);
		return this;
	}
	
	public Portal setSize(Vector2d size) {
		this.size = size;
		return this;
	}
	
	public Portal setRotation(double x, double y) {
		this.rotation = new Vector2d(x, y);
		return this;
	}
	
	public Portal setRotation(Vector2d rotation) {
		this.rotation = rotation;
		return this;
	}
	
	public Portal setNormal(Vector3f normal) {
		this.normal = normal;
		return this;
	}
	
	@Override
	public Vec3 raytraceOffset() {
		return new Vec3(position.x, position.y, position.z);
	}
	
	@Override
	public Quaternion raytraceRotation() {
		return new Quaternion((float) this.rotation.y, (float) this.rotation.x, 0, false);
	}
	
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
		this.normal = first;
	}
	
	@Override
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
		VertexConsumer consumer = source.getBuffer(RenderType.LINES);
		
		/* debug frustum culling box */
//		stack.pushPose();
//		stack.mulPose(new Quaternion(0, (float) -rotation.x, 0, false));
//		stack.translate(-position.x, -position.y, -position.z);
//
//		double c = Math.cos(rotation.x);
//		double s = Math.sin(rotation.x);
//		double halfX = size.x / 2;
//		double x = c * halfX;
//		double z = s * halfX;
//		AABB box = new AABB(
//				position.x - x, position.y, position.z - z,
//				position.x + x, position.y + size.y, position.z + z
//		);
//		LevelRenderer.renderLineBox(stack, consumer, box, 1, 0, 0, 1);
//
//		stack.popPose();
		
		LevelRenderer.renderLineBox(
				stack, consumer,
				-size.x / 2, 0, 0,
				size.x / 2, size.y, 0,
				1, 1, 1, 1
		);
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
	public void setupAsTarget(PoseStack stack) {
		boolean isMirror = target == this;
		Vector3d position = this.position;
		Vector2d rotation = this.rotation;
		// TODO: figure out vertical rotation
		// rotate
//		stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
		stack.mulPose(new Quaternion(0, (float) -rotation.x, 0, false));
		if (isMirror) stack.mulPose(new Quaternion(0, -90, 0, true));
		// TODO: I'm not sure where this 180 is coming from
		if (DynamicPortals.isRotate180Needed()) stack.mulPose(new Quaternion(0, 180, 0, true));
		// translate
		stack.translate(-position.x, -position.y, isMirror ? position.z : -position.z);
		// mirror
		if (isMirror) stack.scale(1, 1, -1);
	}
	
	@Override
	public boolean shouldRender(Frustum frustum, double camX, double camY, double camZ) {
		if (normal == null || normal.dot(new Vector3f((float) (camX - position.x), (float) (camY - position.y), (float) (camZ - position.z))) > 0) {
			// TODO: deal with vertical rotation
			double c = Math.cos(rotation.x);
			double s = Math.sin(rotation.x);
			double halfX = size.x / 2;
			double x = c * halfX;
			double z = s * halfX;
			AABB box = new AABB(
					position.x - x, position.y, position.z - z,
					position.x + x, position.y + size.y, position.z + z
			);
			return frustum.isVisible(box);
		}
		return false;
	}
	
	@Override
	public void setupRenderState() {
		// TODO: check if this works well enough
		if (target == this)
			GL11.glCullFace(GL11.GL_FRONT);
	}
	
	@Override
	public void teardownRenderState() {
		if (target == this)
			GL11.glCullFace(GL11.GL_BACK);
	}
	
	@Override
	public double trace(Vec3 start, Vec3 end) {
		// setup a matrix stack
		PoseStack stack = new PoseStack();
		stack.mulPose(new Quaternion((float) -rotation.y, (float) -rotation.x, 0, false));
		stack.translate(-position.x, -position.y, -position.z);
		// copy to vec4
		Vector4f startVec = new Vector4f((float) start.x, (float) start.y, (float) start.z, 1);
		Vector4f endVec = new Vector4f((float) end.x, (float) end.y, (float) end.z, 1);
		// transform
		startVec.transform(stack.last().pose());
		endVec.transform(stack.last().pose());
		
		// trace
		double dx = endVec.x() - startVec.x();
		double dy = endVec.y() - startVec.y();
		double dz = endVec.z() - startVec.z();
		double[] dist = new double[1];
		dist[0] = 1;
		AABB box = new AABB(-size.x / 2, 0, 0, size.x / 2, size.y, 0);
		AABB.getDirection(
				box, new Vec3(startVec.x(), startVec.y(), startVec.z()), dist,
				null, dx, dy, dz
		);
		return dist[0];
	}
}
