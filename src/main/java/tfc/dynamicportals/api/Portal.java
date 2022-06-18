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
		boolean isMirror = target == this;
		Vector3d position = this.position;
//		if (isMirror) position = this.position;
//		else position = target.position;
		Vector2d rotation = this.rotation;
//		if (isMirror) rotation = this.rotation;
//		else rotation = target.rotation;
		stack.mulPose(new Quaternion(0, (float) rotation.x, 0, false));
		if (!isMirror) stack.mulPose(new Quaternion(0, 90, 0, true));
		stack.translate(-position.x, -position.y, isMirror ? position.z : -position.z);
		// rotate
		// TODO: figure out vertical rotation
//		stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
		// mirror
//		stack.mulPose(new Quaternion(0, 0, 0, true));
//		stack.mulPose(new Quaternion(180, 0, 0, true));
		if (isMirror) stack.scale(1, 1, -1);
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
		PoseStack stack = new PoseStack();
		setupMatrix(stack);
		Vector4f startVec = new Vector4f((float) start.x, (float) start.y, (float) start.z, 1);
		Vector4f endVec = new Vector4f((float) end.x, (float) end.y, (float) end.z, 1);
		startVec.transform(stack.last().pose());
		endVec.transform(stack.last().pose());
		
		AABB box = new AABB(-size.x / 2, 0, 0, size.x / 2, size.y, 0);
		start = new Vec3(startVec.x(), startVec.y(), startVec.z());
		end = new Vec3(endVec.x(), endVec.y(), endVec.z());
//		AABB.getDirection(box, start, end, new BlockPos(0, 0, 0));
		double dx = end.x - start.x;
		double dy = end.y - start.y;
		double dz = end.z - start.z;
		double[] dist = new double[1];
		dist[0] = 1;
		AABB.getDirection(
				box, start, dist,
				null, dx, dy, dz
		);
		return dist[0];
	}
}
