package tfc.dynamicportals.api;

import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import tfc.dynamicportals.DynamicPortals;
import tfc.dynamicportals.util.Quad;
import tfc.dynamicportals.util.VecMath;

public class BasicPortal extends AbstractPortal {
	Vector3d position;
	Vector2d size;
	Vector2d rotation;
	Vector3f normal;
	PortalCamera cam;
	Vec3 compNorm;

	public BasicPortal setPosition(double x, double y, double z) {
		this.position = new Vector3d(x, y, z);
		if (rotation != null) {
			Vector3f oldNorm = normal;
			computeNormal();
			compNorm = new Vec3(normal.x(), normal.y(), normal.z());
			normal = oldNorm;
		}
		return this;
	}

	public BasicPortal setPosition(Vector3d position) {
		this.position = position;
		if (position != null && rotation != null) {
			Vector3f oldNorm = normal;
			computeNormal();
			compNorm = new Vec3(normal.x(), normal.y(), normal.z());
			normal = oldNorm;
		}
		return this;
	}

	public BasicPortal setSize(double x, double y) {
		this.size = new Vector2d(x, y);
		if (position != null && rotation != null) {
			Vector3f oldNorm = normal;
			computeNormal();
			compNorm = new Vec3(normal.x(), normal.y(), normal.z());
			normal = oldNorm;
		}
		return this;
	}

	public BasicPortal setSize(Vector2d size) {
		this.size = size;
		if (position != null && rotation != null) {
			Vector3f oldNorm = normal;
			computeNormal();
			compNorm = new Vec3(normal.x(), normal.y(), normal.z());
			normal = oldNorm;
		}
		return this;
	}

	public BasicPortal setRotation(double x, double y) {
		this.rotation = new Vector2d(x, y);
		if (position != null) {
			Vector3f oldNorm = normal;
			computeNormal();
			compNorm = new Vec3(normal.x(), normal.y(), normal.z());
			normal = oldNorm;
		}
		return this;
	}

	public BasicPortal setRotation(Vector2d rotation) {
		this.rotation = rotation;
		if (position != null && rotation != null) {
			Vector3f oldNorm = normal;
			computeNormal();
			compNorm = new Vec3(normal.x(), normal.y(), normal.z());
			normal = oldNorm;
		}
		return this;
	}

	public BasicPortal setNormal(Vector3f normal) {
		this.normal = normal;
		return this;
	}

	@Override
	public boolean requireTraceRotation() {
		// TODO: I'm not really sure if this is more expensive then just always rotating the look vector
		if (target instanceof BasicPortal) {
			double xRot = ((BasicPortal) target).rotation.x;
			double xr = rotation.x;
			xRot += Math.toRadians(180);
			xRot %= Math.PI * 2;
			xr %= Math.PI * 2;
			if (xr == xRot) {
				double yRot = ((BasicPortal) target).rotation.y;
				if (yRot < 0) yRot = -(-yRot % Math.PI);
				else yRot %= Math.PI;

				double yr = rotation.y;
				if (yr < 0) yr = -(-yr % Math.PI);
				else yr %= Math.PI;

				return yRot != -yr;
			}
		}
		return true;
	}

	@Override
	public Vec3 raytraceOffset() {
		return new Vec3(position.x, position.y, position.z);
	}

	@Override
	public Quaternion raytraceRotation() {
		Quaternion quat;
		Quaternion first = new Quaternion((float) -rotation.y, 0, 0, false);
		Quaternion second = new Quaternion(0, (float) -rotation.x, 0, false);
		quat = second;
		quat.mul(first);
//		quat = new Quaternion(0,0,0,false);
		return quat;
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
		matrix3f.mul(new Quaternion(0, (float) rotation.x, 0, false));
		matrix3f.mul(new Quaternion((float) -rotation.y, 0, 0, false));
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

//		/* debug frustum culling box */
//		stack.pushPose();
//		// absolute position
//		stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
//		stack.mulPose(new Quaternion(0, (float) -rotation.x, 0, false));
//		stack.translate(-position.x, -position.y, -position.z);
//
//		// setup quaternion
//		Quaternion quaternion = new Quaternion((float) rotation.y, 0, 0, false);
//		quaternion.mul(new Quaternion(0, (float) rotation.x, 0, false));
//		// transform
//		Quaternion point = new Quaternion((float) (-size.x / 2), (float) size.y, 0, 1);
//		Quaternion quat = quaternion.copy();
//		point.mul(quat);
//		quat.conj();
//		quat.mul(point);
//		double max = Math.max(Math.abs(quat.i()), Math.abs(quat.k()));
//		AABB box = new AABB(
//				position.x - max, position.y - quat.j(), position.z - max,
//				position.x + max, position.y + quat.j(), position.z + max
//		);
//		// draw
//		LevelRenderer.renderLineBox(stack, consumer, box, 1, 0, 0, 1);
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
		stack.mulPose(new Quaternion(0, (float) rotation.x, 0, false));
		stack.mulPose(new Quaternion((float) -rotation.y, 0, 0, false));
	}

	@Override
	public void setupAsTarget(PoseStack stack) {
		boolean isMirror = target == this;
		Vector3d position = this.position;
		Vector2d rotation = this.rotation;
		// TODO: figure out vertical rotation
		// rotate
		stack.mulPose(new Quaternion((float) -rotation.y, 0, 0, false));
		stack.mulPose(new Quaternion(0, (float) -rotation.x, 0, false));
		if (isMirror) stack.mulPose(new Quaternion(0, -90, 0, true));
		// TODO: I'm not sure where this 180 is coming from
		if (DynamicPortals.isRotate180Needed()) stack.mulPose(new Quaternion(0, 180, 0, true));
		// translate
//		stack.mulPose(new Quaternion(0, 90, 180, true));
//		stack.translate(0, -2, 0);
		stack.translate(-position.x, -position.y, isMirror ? position.z : -position.z);
		// mirror
		if (isMirror) stack.scale(1, 1, -1);
	}

	@Override
	public boolean shouldRender(Frustum frustum, double camX, double camY, double camZ) {
		if (normal == null || normal.dot(new Vector3f((float) (camX - position.x), (float) (camY - position.y), (float) (camZ - position.z))) > 0) {
			if (frustum == null) return true;
			// TODO: this isn't perfect, but for now it works
			Quaternion quaternion = new Quaternion((float) rotation.y, 0, 0, false);
			quaternion.mul(new Quaternion(0, (float) rotation.x, 0, false));

			Quaternion point = new Quaternion((float) (-size.x / 2), (float) size.y, 0, 1);
			Quaternion quat = quaternion.copy();
			point.mul(quat);
			quat.conj();
			quat.mul(point);
			double max = Math.max(Math.abs(quat.i()), Math.abs(quat.k()));
			AABB box = new AABB(
					position.x - max, position.y - quat.j(), position.z - max,
					position.x + max, position.y + quat.j(), position.z + max
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
		stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
		stack.mulPose(new Quaternion(0, (float) -rotation.x, 0, false));
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

	@Override
	public Camera setupCamera(Entity cameraEntity, double camX, double camY, double camZ, Camera gameCamera) {
		// TODO: this is a bit finicky
		// setup
		if (cam == null || !cam.isInitialized() || cam.actualCamera != gameCamera) {
			cam = new PortalCamera(gameCamera);
			cam.setup(cameraEntity.level, cameraEntity, true, false, 0);
		}
		// position
		Vec3 position = gameCamera.getPosition();
		position = position.subtract(raytraceOffset());
		position = position.add(target.raytraceOffset());
		cam.setPosition(position);
		// TODO: rotation
		// block and fog
		cam.cameraBlock = null;
		cam.cameraFog = null;
		cam.cameraBlock = gameCamera.getBlockAtCamera();
		BlockPos.MutableBlockPos pos = (BlockPos.MutableBlockPos) cam.getBlockPosition();
		// setup position
		Vec3 traceOffset = target.raytraceOffset();
		// TODO: center it in the portal
		pos.set(traceOffset.x + 0.5, traceOffset.y + 1, traceOffset.z + 0.5);
		// setup fog type
		FogType type = cam.getFluidInCamera();
		if (type.equals(FogType.NONE)) cam.cameraFog = gameCamera.getFluidInCamera();
		// correct position
		pos.set(position.x, position.y, position.z);
		// tick and return
		cam.tick();
		return cam;
	}

	@Override
	public boolean isInfront(Entity entity, Vec3 position) {
		Vector3f oldNorm = normal;
		normal = new Vector3f((float) compNorm.x, (float) compNorm.y, (float) compNorm.z);
		boolean result =
				shouldRender(null, position.x, position.y, position.z) ||
						shouldRender(null, position.x, entity.getEyePosition(Minecraft.getInstance().getFrameTime()).y, position.z);
		normal = oldNorm;
		return result;
	}

	@Override
	public boolean overlaps(AABB box) {
		Quaternion rotation = raytraceRotation();
		Vec3 vec0 = new Vec3(-size.x / 2, 0, 0);
		vec0 = VecMath.rotate(vec0, rotation);
		Vec3 vec1 = new Vec3(size.x / 2, 0, 0);
		vec1 = VecMath.rotate(vec1, rotation);
		Vec3 vec2 = new Vec3(size.x / 2, size.y, 0);
		vec2 = VecMath.rotate(vec2, rotation);
		Vec3 vec3 = new Vec3(-size.x / 2, size.y, 0);
		vec3 = VecMath.rotate(vec3, rotation);
		Quad plane = new Quad(vec0, vec1, vec2, vec3);
		return plane.overlaps(box.move(-position.x, -position.y, -position.z));
	}

	@Override
	public Vec2 adjustLook(Vec2 vector, boolean reverse) {
		if (reverse)
			// TODO: vertical rotation
			return new Vec2(vector.x, vector.y - (float) Math.toDegrees(rotation.x));
		return new Vec2(vector.x, vector.y + (float) Math.toDegrees(rotation.x));
	}

	// TODO: for some reason, backface teleportation is busted
	@Override
	public boolean moveEntity(Entity entity, Vec3 position, Vec3 motion) {
		if (shouldRender(null, position.x, position.y, position.z)) {
			boolean wasInfront = isInfront(entity, position);
			boolean isInfront = isInfront(entity, position.add(motion));
			if (wasInfront != isInfront) {
//			double angle = rotation.x * 180 / Math.PI;
//			if (angle % 90 == 0) {
//				// TODO: this calculation can be drastically more reliable
//			}
				if (overlaps(entity.getBoundingBox()) || overlaps(entity.getBoundingBox().move(motion))) {
					Quaternion quaternion = raytraceRotation();
					Quaternion other = target.raytraceRotation();

					Vec3 srcOff = raytraceOffset();
					Vec3 dstOff = target.raytraceOffset();

					Vec3 oldPos = new Vec3(entity.xOld, entity.yOld, entity.zOld);
					Vec3 oPos = new Vec3(entity.xo, entity.yo, entity.zo);
					Vec3 pos = position;
					if (target != this) {
						oldPos = VecMath.transform(oldPos, quaternion, other, this != target, false, srcOff, dstOff);
						oPos = VecMath.transform(oPos, quaternion, other, this != target, false, srcOff, dstOff);
						pos = VecMath.transform(pos, quaternion, other, this != target, false, srcOff, dstOff);
					}

					Vec2 vec = entity.getRotationVector();
					Vec2 vecOld = new Vec2(entity.xRotO, entity.yRotO);
//				System.out.println(vec);
					vec = adjustLook(vec, false);
					vecOld = adjustLook(vecOld, false);
					vec = target.adjustLook(vec, true);
					vecOld = target.adjustLook(vecOld, true);
					entity.setXRot(vec.x);
					entity.xRotO = vecOld.x;
					entity.setYRot(vec.y + 180);
					entity.yRotO = vecOld.y + 180;

					motion = VecMath.transform(motion, quaternion, other, false, true, srcOff, dstOff);
					entity.setDeltaMovement(motion);
					if (entity.level.isClientSide) entity.absMoveTo(pos.x, pos.y, pos.z);
					else entity.absMoveTo(pos.x, pos.y, pos.z);
					entity.setDeltaMovement(motion);
					entity.setXRot(vec.x);
					entity.xRotO = vecOld.x;
					entity.setYRot(vec.y + 180);
					entity.yRotO = vecOld.y + 180;
					entity.xo = oPos.x;
					entity.xOld = oldPos.x;
					entity.yo = oPos.y;
					entity.yOld = oldPos.y;
					entity.zo = oPos.z;
					entity.zOld = oldPos.z;

					return true;
				}
			}
		}
		return false;
	}
}
