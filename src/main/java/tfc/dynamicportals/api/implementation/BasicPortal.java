package tfc.dynamicportals.api.implementation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalCamera;
import tfc.dynamicportals.util.Quad;
import tfc.dynamicportals.util.Vec2d;
import tfc.dynamicportals.util.VecMath;
import tfc.dynamicportals.util.support.PehkuiSupport;
import virtuoel.pehkui.api.ScaleData;

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
		if (FMLEnvironment.dist.isClient())
			this.renderer = new BasicPortalRenderer(this);
	}
	
	public Vector3d getPosition() {
		return position;
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
		// EX_TODO: this is a bit finicky
		// lorenzo: why is it luigi :thonk4:
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
		// EX_TODO: rotation
		// lorenzo: what
		// block and fog
		cam.cameraBlock = null;
		cam.cameraFog = null;
		cam.cameraBlock = gameCamera.getBlockAtCamera();
		// EX_TODO: center it in the portal
		// lorenzo: what 2
		// setup position
//		BlockPos.MutableBlockPos pos = (BlockPos.MutableBlockPos) cam.getBlockPosition();
//		Vec3 traceOffset = target.raytraceOffset();
//		pos.set(traceOffset.x + 0.5, traceOffset.y + 1, traceOffset.z + 0.5);
		// correct position
//		pos.set(position.x, position.y, position.z);
		// setup fog type
		FogType type = cam.getFluidInCamera();
		if (type.equals(FogType.NONE)) cam.cameraFog = gameCamera.getFluidInCamera();
		// tick and return
		cam.tick();
		return cam;
	}
	
	@Override
	public boolean isInFront(Entity entity, Vec3 position) {
		// EX_TODO: get this to work with rotated portals
		// lorenzo: seems working to me already?
		return isInFront(position.add(0, entity.getEyeHeight(), 0));
	}
	
	public boolean isInFront(Vec3 vector) {
		return isInFront(vector.x, vector.y, vector.z);
	}
	
	protected boolean isInFront(double camX, double camY, double camZ) {
		return computedNormal == null || computedNormal.dot(new Vec3((camX - position.x), (camY - position.y), (camZ - position.z))) > 0;
	}
	
	@Override
	public boolean overlaps(AABB box) {
		return portalQuad.overlaps(box.move(-position.x, -position.y, -position.z));
	}
	
	@Override
	public void finishMove(Entity entity, Vec3 position, Vec3 motion) {
//		scale(entity, (float) size.y);
	}
	
	public static void scale(Entity entity, float amt) {
		if (ModList.get().isLoaded("pehkui")) {
			ScaleData data = PehkuiSupport.scaleType.get().getScaleData(entity); // TODO: individual scales for x and y
			data.setScale(data.getScale(1) * amt);
			data.setTargetScale(data.getScale());
			data.setScaleTickDelay(0);
		}
	}
	
	// TODO: work some stuff out better on the server, 'cuz currently this can wind up causing the player to collide with millions of blocks acrossed thousands of chunks
	@Override
	public boolean moveEntity(Entity entity, Vec3 position, Vec3 motion) {
		boolean wasInFront = isInFront(entity, position);
		boolean isInFront = isInFront(entity, position.add(motion));
		double distanceEntityToPortal = position.add(motion).distanceTo(raytraceOffset());
		if (wasInFront != isInFront && distanceEntityToPortal < 2) {
			// TODO: this calculation can be drastically more reliable
//
//			double cosine = Mth.cos((float) rotation.x);
//			if (cosine == 0 || cosine == -1 || cosine == 1) {
//				cosine = Mth.cos((float)rotation.y);
//				if (cosine == 0 || cosine == -1 || cosine == 1) {
//					cosine = Mth.cos((float)rotation.z);
//					if (cosine == 0 || cosine == -1 || cosine == 1) {
//						System.out.println(raytraceOffset() + "multiple");
//					}
//				}
//			}
			double raytraceDistance = trace(position, position.add(motion));
			if (raytraceDistance != -1 && distanceEntityToPortal < raytraceDistance || overlaps(entity.getBoundingBox()) || overlaps(entity.getBoundingBox().move(motion))) {
//				scale(entity, (float) (1 / size.y)); // TODO: individual scales for x and y
				
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
				entity.setDeltaMovement(motion);
				// Luigi's TODO: check if this if is actually useful or not
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
						renderer.teleportEntity(entity);
					}
				}
				return true;
			}
		}
		return false;
	}
}
