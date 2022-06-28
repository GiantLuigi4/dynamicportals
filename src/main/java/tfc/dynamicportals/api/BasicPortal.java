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
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.opengl.GL11;
import tfc.dynamicportals.util.Quad;
import tfc.dynamicportals.util.VecMath;

import java.util.UUID;

public class BasicPortal extends AbstractPortal {
	protected Vector3d position;
	protected Vector2d size;
	protected Vec3 rotation;
	protected Vec3 normal;
	protected PortalCamera cam;
	protected Vec3 compNorm;
	AABB box = null;
	
	public BasicPortal(UUID uuid) {
		super(uuid);
	}
	
	public BasicPortal setPosition(double x, double y, double z) {
		this.position = new Vector3d(x, y, z);
		recomputePortal();
		return this;
	}
	
	public BasicPortal setPosition(Vector3d position) {
		this.position = position;
		recomputePortal();
		return this;
	}
	
	public BasicPortal setSize(double x, double y) {
		this.size = new Vector2d(x, y);
		recomputePortal();
		return this;
	}
	
	public BasicPortal setSize(Vector2d size) {
		this.size = size;
		recomputePortal();
		return this;
	}
	
	public BasicPortal setRotation(double x, double y, double z) {
		this.rotation = new Vec3(x, y, z);
		recomputePortal();
		return this;
	}
	
	public BasicPortal setRotation(Vec3 rotation) {
		this.rotation = rotation;
		recomputePortal();
		return this;
	}
	
	protected void recomputePortal() {
		if (position != null && rotation != null) {
			compNorm = _computeNormal();
			
			if (size != null) {
				Quad qd = makeQuad();
				// easier to iterate over a list
				Vec3[] vecs = new Vec3[]{qd.pt0, qd.pt1, qd.pt2, qd.pt3};
				double nx = Double.POSITIVE_INFINITY;
				double ny = Double.POSITIVE_INFINITY;
				double nz = Double.POSITIVE_INFINITY;
				
				double px = Double.NEGATIVE_INFINITY;
				double py = Double.NEGATIVE_INFINITY;
				double pz = Double.NEGATIVE_INFINITY;
				for (Vec3 vec : vecs) {
					nx = Math.min(vec.x, nx);
					ny = Math.min(vec.y, ny);
					nz = Math.min(vec.z, nz);
					
					px = Math.max(vec.x, px);
					py = Math.max(vec.y, py);
					pz = Math.max(vec.z, pz);
				}
				box = new AABB(
						position.x + nx, position.y + ny, position.z + nz,
						position.x + px, position.y + py, position.z + pz
				);
			}
		}
	}
	
	public BasicPortal setNormal(Vec3 normal) {
		this.normal = normal;
		return this;
	}
	
	public boolean requiresTraceRotation() {
		// TODO: I'm not really sure if this is more expensive then just always rotating the look vector
		if (target instanceof BasicPortal) {
			//Rounding because doubles are bad in binary
			double pairXRot = Math.round((Math.toDegrees(((BasicPortal) target).rotation.x) % 360) * 1000.0) / 1000.0;
			double thisXR = Math.round((Math.toDegrees(rotation.x) % 360) * 1000.0) / 1000.0;
			if (pairXRot < 0) pairXRot += 360;
			if (thisXR < 0) thisXR += 360;

//			pairXRot += Math.toRadians(180);
//			pairXRot %= Math.PI * 2;
//			thisXR %= Math.PI * 2;
			if ((thisXR % 180) == (pairXRot % 180) && (thisXR != pairXRot)) {
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
	public Quaternion getWeirdQuat() {
		BasicPortal target = (BasicPortal) this.target;
		Quaternion weird = Quaternion.ONE.copy();
		if (this.rotation.x == 0) {
			if (this.rotation.y == 0) {
				weird.mul(new Quaternion(0, (float) Math.PI, 0, false));
				//weird.mul(new Quaternion(0, 0,(float) -target.rotation.z, false));
			}
		}
		return weird;
	}
	
	@Override
	public Quaternion raytraceRotation() {
		Quaternion quat = Quaternion.ONE.copy();
		quat.mul(new Quaternion(0, (float) rotation.x, 0, false));
		quat.mul(new Quaternion((float) rotation.y, 0, 0, false));
		quat.mul(new Quaternion(0, 0, (float) rotation.z, false));
		if (target == this) quat.mul(new Quaternion(0, 90, 0, true));
		
		return quat;
	}
	
	@Override
	public Quaternion oppositeRaytraceRotation() {
		Quaternion quat = Quaternion.ONE.copy();
//		quat.mul(new Quaternion(1, 0, 0, 0));
		quat.mul(new Quaternion(0, (float) rotation.x, 0, false));
		quat.mul(new Quaternion((float) rotation.y, 0, 0, false));
		quat.mul(new Quaternion(0, 0, (float) rotation.z, false));
//		quat.mul(new Quaternion(1, 0, 0, 0));
		if (target == this) quat.mul(new Quaternion(0, 90, 0, true));
		
		return quat;
	}
	
	protected Vec3 _computeNormal() {
		Quad qd = makeQuad();
		Vector3f a = new Vector3f((float) qd.pt0.x, (float) qd.pt0.y, (float) qd.pt0.z);
		Vector3f b = new Vector3f((float) qd.pt1.x, (float) qd.pt1.y, (float) qd.pt1.z);
		Vector3f c = new Vector3f((float) qd.pt2.x, (float) qd.pt2.y, (float) qd.pt2.z);
		Vector3f d = new Vector3f((float) qd.pt3.x, (float) qd.pt3.y, (float) qd.pt3.z);
		
		Vector3f first = b.copy();
		first.sub(d);
		Vector3f second = c.copy();
		second.sub(d);
		
		first.cross(second);
		first.normalize();
		return new Vec3(first.x(), first.y(), first.z());
	}
	
	public void computeNormal() {
		this.normal = _computeNormal();
	}
	
	@Override
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
		VertexConsumer consumer = source.getBuffer(RenderType.LINES);
		
		if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
			if (Minecraft.getInstance().options.renderDebug) {                    /* normal vec debug */
				// absolute position
				stack.pushPose();
				stack.translate(0, (float) size.y / 2, 0);
				stack.mulPose(new Quaternion(0, 0, (float) rotation.z, false));
				stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
				stack.mulPose(new Quaternion(0, (float) rotation.x, 0, false));
				
				// draw normal vector
				if (normal != null) {
					consumer.vertex(stack.last().pose(), 0, 0, 0).color(0f, 1, 0, 1).normal(0, 0, 0).endVertex();
					consumer.vertex(stack.last().pose(), (float) normal.x(), (float) normal.y(), (float) normal.z()).color(0f, 1, 0, 1).normal(0, 0, 0).endVertex();
				} else if (compNorm != null) {
					consumer.vertex(stack.last().pose(), 0, 0, 0).color(1f, 0, 1, 1).normal(0, 0, 0).endVertex();
					consumer.vertex(stack.last().pose(), (float) compNorm.x(), (float) compNorm.y(), (float) compNorm.z()).color(1f, 0, 1, 1).normal(0, 0, 0).endVertex();
				}
				
				stack.popPose();
				
				Vec3 norm = compNorm;
				if (norm == null) norm = normal;
				if (norm != null) {
					stack.pushPose();
					stack.translate(0, (float) size.y / 2, 1);
					
					stack.mulPose(new Quaternion(0, 0, (float) rotation.z, false));
					stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
					stack.mulPose(new Quaternion(0, (float) rotation.x, 0, false));
					
					Quaternion quaternion = quadQuat();
					// luigi: lorenzo wanted this
					
					//WHAT DO YOU MEAN I DON'T EVEN KNOW WHAT THIS IS
					// luigi: https://cdn.discordapp.com/attachments/988184753255624774/991124702196154419/unknown.png
					// left vector
					Vec3 vec = VecMath.rotate(new Vec3(1, 0, 0), quaternion);
					consumer.vertex(stack.last().pose(), 0, 0, 0).color(1f, 0.5f, 0, 1).normal(0, 0, 0).endVertex();
					consumer.vertex(stack.last().pose(), (float) vec.x, (float) vec.y, (float) vec.z).color(1f, 0.5f, 0, 1).normal(0, 0, 0).endVertex();
					
					stack.popPose();
				}
			}
			
			/* debug frustum culling box */
			stack.pushPose();
			
			stack.mulPose(new Quaternion(0, 0, (float) rotation.z, false));
			stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
			stack.mulPose(new Quaternion(0, (float) rotation.x, 0, false));
			
			if (Minecraft.getInstance().options.renderDebug) {
				Quad qd = makeQuad();
				consumer.vertex(stack.last().pose(), (float) qd.pt0.x, (float) qd.pt0.y, (float) qd.pt0.z).color(1f, 1, 0, 1).normal(0, 0, 0).endVertex();
				consumer.vertex(stack.last().pose(), (float) qd.pt1.x, (float) qd.pt1.y, (float) qd.pt1.z).color(1f, 1, 0, 1).normal(0, 0, 0).endVertex();
				
				consumer.vertex(stack.last().pose(), (float) qd.pt1.x, (float) qd.pt1.y, (float) qd.pt1.z).color(1f, 1, 0, 1).normal(0, 0, 0).endVertex();
				consumer.vertex(stack.last().pose(), (float) qd.pt2.x, (float) qd.pt2.y, (float) qd.pt2.z).color(1f, 1, 0, 1).normal(0, 0, 0).endVertex();
				
				consumer.vertex(stack.last().pose(), (float) qd.pt2.x, (float) qd.pt2.y, (float) qd.pt2.z).color(1f, 1, 0, 1).normal(0, 0, 0).endVertex();
				consumer.vertex(stack.last().pose(), (float) qd.pt3.x, (float) qd.pt3.y, (float) qd.pt3.z).color(1f, 1, 0, 1).normal(0, 0, 0).endVertex();
				
				consumer.vertex(stack.last().pose(), (float) qd.pt3.x, (float) qd.pt3.y, (float) qd.pt3.z).color(1f, 1, 0, 1).normal(0, 0, 0).endVertex();
				consumer.vertex(stack.last().pose(), (float) qd.pt0.x, (float) qd.pt0.y, (float) qd.pt0.z).color(1f, 1, 0, 1).normal(0, 0, 0).endVertex();
				
				AABB box = Minecraft.getInstance().cameraEntity.getBoundingBox();
				Vec3 center = box.getCenter();
				center = center.subtract(position.x, position.y, position.z);
				Vec3 nearest = qd.getNearest(center);
				if (nearest != null) {
					LevelRenderer.renderLineBox(stack, consumer,
							nearest.x - 0.01, nearest.y - 0.01, nearest.z - 0.01,
							nearest.x + 0.01, nearest.y + 0.01, nearest.z + 0.01,
							0, 0, 1, 1
					);
				}
				
				Quaternion srcQuat = raytraceRotation();
				Quaternion dstQuat = target.oppositeRaytraceRotation();
				Vec3 srcOff = raytraceOffset();
				Vec3 dstOff = target.raytraceOffset();
				Vec3 pos1 = Minecraft.getInstance().cameraEntity.getPosition(1);
				Vec3 srcPos = pos1;
				pos1 = VecMath.old_transform(pos1, srcQuat, dstQuat, this != target, false, srcOff, dstOff);
				
				box = box.move(-srcPos.x, -srcPos.y, -srcPos.z);
				box = box.move(pos1.x, pos1.y, pos1.z);
				
				stack.pushPose();
				stack.translate(-position.x, -position.y, -position.z);
				LevelRenderer.renderLineBox(stack, consumer, box, 0, 0, 1, 1);
				center = box.getCenter();
				Vec3 motion = VecMath.old_transform(Minecraft.getInstance().cameraEntity.getDeltaMovement(), srcQuat, dstQuat, false, true, Vec3.ZERO, Vec3.ZERO);
				
				stack.translate(center.x, center.y, center.z);
				consumer.vertex(stack.last().pose(), 0, 0, 0).color(1f, 1, 1, 1).normal(0, 0, 0).endVertex();
				stack.scale(10, 10, 10);
				consumer.vertex(stack.last().pose(), (float) motion.x, (float) motion.y, (float) motion.z).color(0, 0, 0, 1).normal(0, 0, 0).endVertex();
				
				stack.popPose();
			}
			
			stack.translate(-position.x, -position.y, -position.z);
			
			// draw
			if (box != null)
				LevelRenderer.renderLineBox(stack, consumer, box, 1, 0, 0, 1);
			stack.popPose();
		}
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
		stack.mulPose(new Quaternion(0, (float) -rotation.x, 0, false));
		stack.mulPose(new Quaternion((float) -rotation.y, 0, 0, false));
		stack.mulPose(new Quaternion(0, 0, (float) -rotation.z, false));
	}
	
	@Override
	public void setupAsTarget(PoseStack stack) {
		boolean isMirror = target == this;
		Vector3d position = this.position;
		Vec3 rotation = this.rotation;
		// rotate
		stack.mulPose(new Quaternion(0, 0, (float) rotation.z, false));
		stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
		stack.mulPose(new Quaternion(0, (float) rotation.x, 0, false));
		if (isMirror) stack.mulPose(new Quaternion(0, 180, 0, true));
		// I don't really know why mirrors need this rotation
		// translate
		stack.translate(-position.x, -position.y, isMirror ? position.z : -position.z);
		// mirror
		if (isMirror) stack.scale(1, 1, -1);
	}
	
	@Override
	public boolean shouldRender(Frustum frustum, double camX, double camY, double camZ) {
		if (normal == null || normal.dot(new Vec3((camX - position.x), (camY - position.y), (camZ - position.z))) > 0) {
			if (frustum == null) return true;
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
//		stack.mulPose(new Quaternion(0, 0, (float) rotation.z, false));
//		stack.mulPose(new Quaternion((float) rotation.y, 0, 0, false));
//		stack.mulPose(new Quaternion(0, (float) -rotation.x, 0, false));
		stack.mulPose(quadQuat());
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
	public boolean isInFront(Entity entity, Vec3 position) {
		// TODO: get this to work with rotated portals
		return _isInFront(position.x, position.y + entity.getEyeHeight(), position.z);
	}
	
	public boolean isInFront(Vec3 cam) {
		return _isInFront(cam.x, cam.y, cam.z);
	}
	
	protected boolean _isInFront(double camX, double camY, double camZ) {
		return compNorm == null || compNorm.dot(new Vec3((camX - position.x), (camY - position.y), (camZ - position.z))) > 0;
	}
	
	Quaternion quadQuat() {
		Quaternion quaternion = Quaternion.ONE.copy();
		quaternion.mul(new Quaternion(0, 0, (float) rotation.z, false));
		quaternion.mul(new Quaternion((float) rotation.y, 0, 0, false));
		quaternion.mul(new Quaternion(0, (float) rotation.x, 0, false));
		return quaternion;
	}
	
	protected Quad makeQuad() {
		Quaternion rotation = quadQuat();
		Vec3 vec0 = new Vec3(-size.x / 2, 0, 0);
		vec0 = VecMath.rotate(vec0, rotation);
		Vec3 vec1 = new Vec3(size.x / 2, 0, 0);
		vec1 = VecMath.rotate(vec1, rotation);
		Vec3 vec2 = new Vec3(size.x / 2, size.y, 0);
		vec2 = VecMath.rotate(vec2, rotation);
		Vec3 vec3 = new Vec3(-size.x / 2, size.y, 0);
		vec3 = VecMath.rotate(vec3, rotation);
		Quad plane = new Quad(vec0, vec1, vec2, vec3);
		return plane;
	}
	
	@Override
	public boolean overlaps(AABB box) {
		Quad plane = makeQuad();
//		Vec3 sizeVec = new Vec3(size.x / 2, 0, 0);
//		sizeVec = VecMath.rotate(sizeVec, rotation);
//		return plane.overlaps(box.move(-position.x + sizeVec.x, -position.y, -position.z + sizeVec.z));
		return plane.overlaps(box.move(-position.x, -position.y, -position.z));
	}
	
	private float clamp(float horiz) {
		float v = horiz;
		if (v < 0) v += 360;
//		v += 180;
		v %= 360;
//		v -= 180;
		return v;
	}
	
	@Override
	public Vec2 adjustLook(Vec2 vector, boolean reverse) {
		if (reverse)
			// TODO: vertical rotation
			return new Vec2(vector.x, clamp(vector.y - (float) Math.toDegrees(rotation.x)));
		return new Vec2(vector.x, clamp(vector.y + (float) Math.toDegrees(rotation.x)));
	}
	
	// TODO: work some stuff out better on the server, 'cuz currently this can wind up causing the player to collide with millions of blocks acrossed thousands of chunks
	@Override
	public boolean moveEntity(Entity entity, Vec3 position, Vec3 motion) {
		boolean wasInFront = isInFront(entity, position);
		boolean isInFront = isInFront(entity, position.add(motion));
		if (wasInFront != isInFront) {
//			double angle = rotation.x * 180 / Math.PI;
//			if (angle % 90 == 0) {
//				// TODO: this calculation can be drastically more reliable
//			}
			if (overlaps(entity.getBoundingBox()) || overlaps(entity.getBoundingBox().move(motion))) {
				Quaternion srcQuat = raytraceRotation();
				Quaternion dstQuat = target.oppositeRaytraceRotation();
				Vec3 srcOff = raytraceOffset();
				Vec3 dstOff = target.raytraceOffset();
				
				Vec3 oldPos = new Vec3(entity.xOld, entity.yOld, entity.zOld);
				Vec3 oPos = new Vec3(entity.xo, entity.yo, entity.zo);
				Vec3 pos = position;
				if (target != this) {
					oldPos = VecMath.old_transform(oldPos, srcQuat, dstQuat, this != target, false, srcOff, dstOff);
					oPos = VecMath.old_transform(oPos, srcQuat, dstQuat, this != target, false, srcOff, dstOff);
					pos = VecMath.old_transform(pos, srcQuat, dstQuat, this != target, false, srcOff, dstOff);
//					interpStart = VecMath.start_transform(interpStart, srcQuat, dstQuat, portal == portal.target, false, srcOff, dstOff);
//					interpReach = VecMath.start_transform(interpReach, srcQuat, dstQuat, portal == portal.target, true, Vec3.ZERO, Vec3.ZERO);
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
				
				motion = VecMath.old_transform(motion, srcQuat, dstQuat, false, true, Vec3.ZERO, Vec3.ZERO);
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
				
				if (entity.level.isClientSide) {
					if (FMLEnvironment.dist.isClient()) {
						// TODO: check if it's an instance of a client world
						// TODO: shift call out of "common" code
						if (entity == Minecraft.getInstance().cameraEntity) {
							if (graph != null)
								graph.nudgeRenderer();
						}
					}
				}
				return true;
			}
		}
		return false;
	}
}
