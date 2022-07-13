package tfc.dynamicportals.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;
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
import tfc.dynamicportals.GLUtils;
import tfc.dynamicportals.Renderer;
import tfc.dynamicportals.util.Quad;
import tfc.dynamicportals.util.Vec2d;
import tfc.dynamicportals.util.VecMath;

import java.util.UUID;

public class BasicPortal extends AbstractPortal {
	protected Vector3d position;
	protected Vec2d size;
	protected Vec3 rotation;
	protected Vec3 renderNormal;
	protected Vec3 computedNormal;
	protected PortalCamera cam;
	protected Quad portalQuad;
	AABB box = null;
	
	public BasicPortal(UUID uuid) {
		super(uuid);
	}
	
	public BasicPortal setPosition(double x, double y, double z) {
		this.position = new Vector3d(x, y, z);
		recomputePortal();
		return this;
	}
	
	public BasicPortal setSize(double x, double y) {
		this.size = new Vec2d(x, y);
		recomputePortal();
		return this;
	}
	
	public BasicPortal setRotation(double x, double y, double z) {
		this.rotation = new Vec3(x, y, z);
		recomputePortal();
		return this;
	}
	
	public BasicPortal setRenderNormal(Vec3 renderNormal) {
		this.renderNormal = renderNormal;
		return this;
	}
	
	public BasicPortal setPosition(Vec3 pos) {
		return this.setPosition(pos.x, pos.y, pos.z);
	}
	
	public BasicPortal setSize(Vec2d size) {
		return this.setSize(size.x, size.y);
	}
	
	public BasicPortal setRotation(Vec3 rotation) {
		return this.setRotation(rotation.x, rotation.y, rotation.z);
	}
	
	public void computeRenderNormal() {
		this.setRenderNormal(portalQuad.normal());
	}
	
	protected void recalculateQuad() {
		Quaternion rotation = getActualRotation();
		Vec3 vec0 = VecMath.rotate(new Vec3(-size.x / 2, 0, 0), rotation);
		Vec3 vec1 = VecMath.rotate(new Vec3(size.x / 2, 0, 0), rotation);
		Vec3 vec2 = VecMath.rotate(new Vec3(size.x / 2, size.y, 0), rotation);
		Vec3 vec3 = VecMath.rotate(new Vec3(-size.x / 2, size.y, 0), rotation);
		this.portalQuad = new Quad(vec0, vec1, vec2, vec3);
	}
	
	protected void recomputePortal() {
		if (position != null && rotation != null) {
			recalculateQuad();
			computedNormal = portalQuad.normal();
			
			if (size != null) {
				Vec3[] vertices = new Vec3[]{portalQuad.pt0, portalQuad.pt1, portalQuad.pt2, portalQuad.pt3};
				double nx = Double.POSITIVE_INFINITY;
				double ny = Double.POSITIVE_INFINITY;
				double nz = Double.POSITIVE_INFINITY;
				
				double px = Double.NEGATIVE_INFINITY;
				double py = Double.NEGATIVE_INFINITY;
				double pz = Double.NEGATIVE_INFINITY;
				for (Vec3 vert : vertices) {
					nx = Math.min(vert.x, nx);
					ny = Math.min(vert.y, ny);
					nz = Math.min(vert.z, nz);
					
					px = Math.max(vert.x, px);
					py = Math.max(vert.y, py);
					pz = Math.max(vert.z, pz);
				}
				box = new AABB(
						position.x + nx, position.y + ny, position.z + nz,
						position.x + px, position.y + py, position.z + pz
				);
			}
		}
	}
	
	@Override
	public Vec3 raytraceOffset() {
		return new Vec3(position.x, position.y, position.z);
	}

//	public boolean requiresTraceRotation() {
//		// TODO: I'm not really sure if this is more expensive then just always rotating the look vector
//		if (target instanceof BasicPortal) {
//			//Rounding because doubles are bad in binary
//			double pairXRot = Math.round((Math.toDegrees(((BasicPortal) target).rotation.x) % 360) * 1000.0) / 1000.0;
//			double thisXR = Math.round((Math.toDegrees(rotation.x) % 360) * 1000.0) / 1000.0;
//			if (pairXRot < 0) pairXRot += 360;
//			if (thisXR < 0) thisXR += 360;
//
////			pairXRot += Math.toRadians(180);
////			pairXRot %= Math.PI * 2;
////			thisXR %= Math.PI * 2;
//			if ((thisXR % 180) == (pairXRot % 180) && (thisXR != pairXRot)) {
//				double yRot = ((BasicPortal) target).rotation.y;
//				if (yRot < 0) yRot = -(-yRot % Math.PI);
//				else yRot %= Math.PI;
//
//				double yr = rotation.y;
//				if (yr < 0) yr = -(-yr % Math.PI);
//				else yr %= Math.PI;
//
//				return yRot != -yr;
//			}
//		}
//		return true;
//	}
	
	@Override
	public Quaternion raytraceRotation() {
		Quaternion rot = Quaternion.fromYXZ((float) -rotation.x, (float) -rotation.y, (float) -rotation.z);
		if (target == this) rot.mul(new Quaternion(0, 90, 0, true));
		return rot;
	}
	
	@Override
	public Vec3 getScaleRatio() {
		return this.target == this ? new Vec3(1, 1, 1) : new Vec3(target.getSize().x / this.size.x, target.getSize().y / this.size.y, target.getSize().x / this.size.x);
	}
	
	@Override
	public Vec2d getSize() {
		return size;
	}
	
	@Override
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
		VertexConsumer consumer = source.getBuffer(RenderType.LINES);
		
		if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
			if (Minecraft.getInstance().options.renderDebug) {                    /* normal vec debug */
				// absolute position
				stack.pushPose();
				stack.translate(0, (float) size.y / 2, 0);
				stack.mulPose(getActualRotation());
				
				// draw normal vector
				if (renderNormal != null) {
					Renderer.renderVector(stack, consumer, renderNormal, 0, 1, 0);
				} else if (computedNormal != null) {
					Renderer.renderVector(stack, consumer, computedNormal, 1, 0, 1);
				}
				stack.popPose();
				
				//It's actually wrong
//				Vec3 norm = computedNormal;
//				if (norm == null) norm = normal;
//				if (norm != null) {
//					stack.pushPose();
//					stack.translate(0, (float) size.y / 2, 1);
//
//					Quaternion quaternion = getActualRotation();
//					stack.mulPose(quaternion);
//					// luigi: lorenzo wanted this
//
//					//WHAT DO YOU MEAN I DON'T EVEN KNOW WHAT THIS IS
//					// luigi: https://cdn.discordapp.com/attachments/988184753255624774/991124702196154419/unknown.png
//					// left vector
//
//					// I don't need it anymore, you can remove it luigi
//					// luigi: nah, I'm keeping it
//					Vec3 vec = VecMath.rotate(new Vec3(1, 0, 0), quaternion);
//					consumer.vertex(stack.last().pose(), 0, 0, 0).color(1f, 0.5f, 0, 1).normal(1, 0, 0).endVertex();
//					consumer.vertex(stack.last().pose(), (float) vec.x, (float) vec.y, (float) vec.z).color(1f, 0.5f, 0, 1).normal(1, 0, 0).endVertex();
//
//					stack.popPose();
//				}
			}
			
			/* debug frustum culling box */
			stack.pushPose();
			stack.mulPose(getActualRotation());
			
			if (Minecraft.getInstance().options.renderDebug) {
				Quad qd = portalQuad;
				Renderer.renderVector(stack, consumer, qd.pt0, qd.pt1, 1, 0, 0);
				Renderer.renderVector(stack, consumer, qd.pt1, qd.pt2, 1, 1, 0);
				Renderer.renderVector(stack, consumer, qd.pt2, qd.pt3, 1, 0, 1);
				Renderer.renderVector(stack, consumer, qd.pt3, qd.pt0, 0, 1, 1);
				{
					Vec3 eye = Minecraft.getInstance().cameraEntity.getEyePosition();
					eye = eye.subtract(position.x, position.y, position.z);
					Vec3 nearestInQuad = qd.nearestInQuad(eye);
					Vec3 nearestOnEdge = qd.nearestOnEdge(eye);
					Vec3 nearest = qd.nearest(eye);
					Vec3 mid = qd.center();
					double size = 0.005;
					Renderer.renderPoint(stack, consumer, mid, size, 1, 1, 1);
					if (nearestOnEdge != null && nearest != null) {
						if (nearestOnEdge.distanceToSqr(mid) <= nearest.distanceToSqr(mid)) {
							Renderer.renderPoint(stack, consumer, nearestOnEdge, size, 1, 0, nearestInQuad != null ? 1 : 0);
						} else {
							Renderer.renderPoint(stack, consumer, nearest, size, 0, nearestInQuad != null ? 1 : 0, nearestInQuad == null ? 1 : 0);
						}
					} else if (nearest != null) {
						Renderer.renderPoint(stack, consumer, nearest, size, 0, nearestInQuad != null ? 1 : 0, nearestInQuad == null ? 1 : 0);
					}
				}
				
				if (this != target) {
					//  entity bounding box
					AABB box = Minecraft.getInstance().cameraEntity.getBoundingBox();
					Vec3 center = box.getCenter();
//					center = center.subtract(position.x, position.y, position.z);
					
					// all the vars
					Quaternion srcQuat = raytraceRotation();
					Quaternion dstQuat = target.raytraceRotation();
					Vec3 srcOff = raytraceOffset();
					Vec3 dstOff = target.raytraceOffset();
					Vec3 pos1 = Minecraft.getInstance().cameraEntity.getPosition(1);
					Vec3 srcPos = pos1;
					pos1 = VecMath.transform(pos1, srcQuat, dstQuat, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), this != target, srcOff, dstOff);
					
					box = box.move(-srcPos.x, -srcPos.y, -srcPos.z);
					box = box.move(pos1.x, pos1.y, pos1.z);
					
					stack.pushPose();
					stack.translate(-position.x, -position.y, -position.z);
					LevelRenderer.renderLineBox(stack, consumer, box, 0, 0, 1, 1);
					center = box.getCenter();
					Vec3 motion = VecMath.transform(Minecraft.getInstance().cameraEntity.getDeltaMovement(), srcQuat, dstQuat, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, Vec3.ZERO, Vec3.ZERO);
					stack.translate(center.x, center.y, center.z);
					consumer.vertex(stack.last().pose(), 0, 0, 0).color(1f, 1, 1, 1).normal(1, 0, 0).endVertex();
					stack.scale(10, 10, 10);
					consumer.vertex(stack.last().pose(), (float) motion.x, (float) motion.y, (float) motion.z).color(0, 0, 0, 1).normal(1, 0, 0).endVertex();
					
					stack.popPose();
				}
			}
			
			stack.translate(-position.x, -position.y, -position.z);
			
			// draw
			if (box != null)
				LevelRenderer.renderLineBox(stack, consumer, box.inflate(Minecraft.getInstance().options.renderDebug ? 0.01 : 0), 1, 0, 0, 1);
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
		Quaternion quaternion = raytraceRotation();
		if (target == this) quaternion.mul(new Quaternion(0, -90, 0, true));
		stack.mulPose(quaternion);
	}
	
	@Override
	public void fullSetupMatrix(PoseStack stack) {
		this.setupMatrix(stack);
		
		float xScl = (float) size.x;
		float yScl = (float) size.y;
		stack.scale(xScl, yScl, xScl);
	}
	
	@Override
	public void setupAsTarget(PoseStack stack) {
		float xScl = 1f / (float) size.x;
		float yScl = 1f / (float) size.y;
		
		stack.scale(xScl, yScl, xScl);
		
		boolean isMirror = target == this;
		Vector3d position = this.position;
		// rotate
		
		if (isMirror) {
			// mirror
			stack.scale(1, 1, -1);
			// I don't really know why mirrors need this rotation
			stack.mulPose(new Quaternion(0, 180, 0, true));
		}
		stack.mulPose(getActualRotation());
		// translate
		stack.translate(-position.x, -position.y, -position.z);
	}
	
	@Override
	public boolean shouldRender(Frustum frustum, double camX, double camY, double camZ) {
		if (renderNormal == null || renderNormal.dot(new Vec3((camX - position.x), (camY - position.y), (camZ - position.z))) > 0) {
			if (frustum == null) return true;
			return frustum.isVisible(box);
		}
		return false;
	}
	
	@Override
	public void setupRenderState() {
		// TODO: check if this works well enough
		if (this == target)
			GLUtils.swapBackface(true);
	}
	
	@Override
	public void teardownRenderState() {
		if (this == target)
			GLUtils.swapBackface(false);
	}
	
	@Override
	public double trace(Vec3 start, Vec3 end) {
		// setup a matrix stack
		PoseStack stack = new PoseStack();
		stack.mulPose(getActualRotation());
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
		return isInFront(position.add(0, entity.getEyeHeight(), 0));
	}
	
	public boolean isInFront(Vec3 vector) {
		return _isInFront(vector.x, vector.y, vector.z);
	}
	
	protected boolean _isInFront(double camX, double camY, double camZ) {
		return computedNormal == null || computedNormal.dot(new Vec3((camX - position.x), (camY - position.y), (camZ - position.z))) > 0;
	}
	
	@Override
	public boolean overlaps(AABB box) {
		return portalQuad.overlaps(box.move(-position.x, -position.y, -position.z));
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
				Quaternion dstQuat = target.raytraceRotation();
				Vec3 srcOff = raytraceOffset();
				Vec3 dstOff = target.raytraceOffset();
				
				Vec3 oldPos = new Vec3(entity.xOld, entity.yOld, entity.zOld);
				Vec3 oPos = new Vec3(entity.xo, entity.yo, entity.zo);
				Vec3 pos = position;
				if (target != this) {
					oldPos = VecMath.transform(oldPos, srcQuat, dstQuat, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, srcOff, dstOff);
					oPos = VecMath.transform(oPos, srcQuat, dstQuat, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, srcOff, dstOff);
					pos = VecMath.transform(pos, srcQuat, dstQuat, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, srcOff, dstOff);
				}
				
				Vec3 look = VecMath.getLookVec(entity.getRotationVector());
				look = VecMath.transform(look, srcQuat, dstQuat, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), this == target, Vec3.ZERO, Vec3.ZERO);
				Vec2 rotVec = VecMath.lookAngle(look);
				
				Vec3 oldLook = VecMath.getLookVec(entity.getRotationVector());
				oldLook = VecMath.transform(oldLook, srcQuat, dstQuat, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), this == target, Vec3.ZERO, Vec3.ZERO);
				Vec2 oldRotVec = VecMath.lookAngle(oldLook);
				
				entity.setXRot(rotVec.x);
				entity.xRotO = oldRotVec.x;
				entity.setYRot(rotVec.y);
				entity.yRotO = oldRotVec.y;
				
				motion = VecMath.transform(motion, srcQuat, dstQuat, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), target == this, Vec3.ZERO, Vec3.ZERO);
				double scl = motion.length();
//				motion = motion.scale(-1);
				motion = motion.normalize().scale(scl);
				entity.setDeltaMovement(motion);
				if (entity.level.isClientSide) entity.absMoveTo(pos.x, pos.y, pos.z);
				else entity.absMoveTo(pos.x, pos.y, pos.z);
				entity.setDeltaMovement(motion);
				
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
