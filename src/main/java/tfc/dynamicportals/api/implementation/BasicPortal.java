package tfc.dynamicportals.api.implementation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;
import com.tracky.TrackyAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.implementation.data.PortalDataSerializers;
import tfc.dynamicportals.api.implementation.data.PortalDataTracker;
import tfc.dynamicportals.api.implementation.data.PortalTrackedData;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.access.ParticleAccessor;
import tfc.dynamicportals.util.Quad;
import tfc.dynamicportals.util.TrackyTools;
import tfc.dynamicportals.util.Vec2d;
import tfc.dynamicportals.util.VecMath;
import tfc.dynamicportals.util.support.PehkuiSupport;
import virtuoel.pehkui.api.ScaleData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// TODO: TrackablePortal interface?
public class BasicPortal extends AbstractPortal {
	protected Vector3d position;
	protected Vec2d size;
	protected Vec3 rotation;
	protected Vec3 renderNormal;
	protected Vec3 computedNormal;
	protected Quad portalQuad;
	public final PortalType<?> type;
	AABB box = null;
	
	public final PortalDataTracker tracker = new PortalDataTracker();
	
	public BasicPortal(UUID uuid) {
		this(uuid, BasicPortalTypes.BASIC);
	}
	
	public BasicPortal(UUID uuid, PortalType<?> type) {
		super(uuid);
		if (FMLEnvironment.dist.isClient())
			this.renderer = new BasicPortalRenderer(this);
		this.type = type;
		registerTrackedData();
	}
	
	PortalTrackedData<Vector3d> POSITION = new PortalTrackedData<>("position", PortalDataSerializers.VECTOR_3D);
	PortalTrackedData<Vec2d> SIZE = new PortalTrackedData<>("size", PortalDataSerializers.VEC2D);
	PortalTrackedData<Vec3> ROTATION = new PortalTrackedData<>("rotation", PortalDataSerializers.VEC3);
	PortalTrackedData<Optional<Vec3>> RENDER_NORM = new PortalTrackedData<>("render_normal", PortalDataSerializers.OPTIONAL_VEC3);
	
	protected void registerTrackedData() {
		tracker.register(POSITION, () -> position, (pos) -> setPosition(pos.x, pos.y, pos.z));
		tracker.register(SIZE, () -> size, (pos) -> setSize(pos.x, pos.y));
		tracker.register(ROTATION, () -> rotation, this::setRotation);
		tracker.register(RENDER_NORM, () -> {
			if (renderNormal != null) return Optional.of(renderNormal);
			else return Optional.empty();
		}, (vec) -> vec.ifPresent(this::setRenderNormal));
	}
	
	public static void scale(Entity entity, float amt) {
		if (ModList.get().isLoaded("pehkui")) {
			// Luigi's TODO: individual scales for x and y
			ScaleData data = PehkuiSupport.scaleType.get().getScaleData(entity);
			data.setScale(data.getScale(1) * amt);
			data.setTargetScale(data.getScale());
			data.setScaleTickDelay(0);
		}
	}
	
	public BasicPortal setPosition(Vec3 pos) {
		return this.setPosition(pos.x, pos.y, pos.z);
	}
	
	public BasicPortal setPosition(double x, double y, double z) {
		this.position = new Vector3d(x, y, z);
		recomputePortal();
		tracker.update(POSITION);
		return this;
	}
	
	public BasicPortal setSize(double x, double y) {
		this.size = new Vec2d(x, y);
		recomputePortal();
		tracker.update(SIZE);
		return this;
	}
	
	public BasicPortal setRotation(double x, double y, double z) {
		this.rotation = new Vec3(x, y, z);
		recomputePortal();
		tracker.update(ROTATION);
		return this;
	}
	
	public BasicPortal setRenderNormal(Vec3 renderNormal) {
		this.renderNormal = renderNormal;
		tracker.update(RENDER_NORM);
		return this;
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
	
	public BasicPortal setSize(Vec2d size) {
		return this.setSize(size.x, size.y);
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
	public boolean isInFront(Entity entity, Vec3 position) {
		return isInFront(position.add(0, entity.getEyeHeight(), 0));
	}

	@Override
	public boolean canTeleport(Entity entity, Vec3 position) {
		double distanceEntityToPortal = position.distanceTo(portalQuad.center().add(raytraceOffset()));
		// lorenzo: that "less than" check is a temporary hack to avoid calling this method for portals 20 blocks away
		// luigi: not really temporary, actually
		// lorenzo: alright, fixed the check
		double reach;
		if (entity instanceof LivingEntity) {
			AttributeInstance instance = ((LivingEntity) entity).getAttribute(ForgeMod.REACH_DISTANCE.get());
			if (instance == null) reach = 0;
			else reach = instance.getValue();
		} else reach = 0;
		
		if (distanceEntityToPortal < reach) {
			if (renderNormal != null)
				return isInFront(entity, position);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canTeleport(Vec3 position) {
		double distanceEntityToPortal = position.distanceTo(portalQuad.center().add(raytraceOffset()));
		if (distanceEntityToPortal < 1) {
			if (renderNormal != null)
				return isInFront(position);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isInFront(Vec3 vector) {
		return isInFront(vector.x, vector.y, vector.z);
	}
	
	protected boolean isInFront(double camX, double camY, double camZ) {
		return computedNormal == null || computedNormal.dot(new Vec3((camX - position.x), (camY - position.y), (camZ - position.z))) > 0;
	}
	
	@Override
	public boolean overlaps(AABB box) {
		if (this.box.intersects(box)) {
			return portalQuad.overlaps(box.move(-position.x, -position.y, -position.z));
		}
		return false;
	}
	
	@Override
	public void finishMove(Entity entity, Vec3 position, Vec3 motion) {
//		scale(entity, (float) size.y);
	}
	
	// Luigi's TODO: work some stuff out better on the server, 'cuz currently this can wind up causing the player to collide with millions of blocks acrossed thousands of chunks
	@Override
	public boolean moveEntity(Entity entity, Vec3 position, Vec3 motion) {
		boolean wasInFront = isInFront(entity, position);
		boolean isInFront = isInFront(entity, position.add(motion));
		if (wasInFront != isInFront) {
//			// TODO: do stuff with this
//			Vec3 rot = VecMath.toDeegrees(rotation);
//			if ((((int) (rot.x * 3)) / 3) % 90 == 0 && (((int) (rot.y * 3)) / 3) % 90 == 0 && (((int) (rot.z * 3)) / 3) % 90 == 0) {
//				double x = RaytraceHelper.calculateXOffset(box, entity.getBoundingBox(), motion.x);
//				double y = RaytraceHelper.calculateYOffset(box, entity.getBoundingBox(), motion.y);
//				double z = RaytraceHelper.calculateZOffset(box, entity.getBoundingBox(), motion.z);
//				if (x != motion.x || y != motion.y || z != motion.z)
//					System.out.println(raytraceOffset() + "multiple");
//			}
			
			double raytraceDistance = trace(position, position.add(motion));
			// luigi: not sure if this comparison between raytrace distance and distance from entity to portal is a good idea or not
			double distanceEntityToPortal = position.distanceTo(portalQuad.center().add(raytraceOffset()));
			if (raytraceDistance != -1 && distanceEntityToPortal < raytraceDistance || overlaps(entity.getBoundingBox()) || overlaps(entity.getBoundingBox().move(motion))) {
				// Luigi's TODO: individual scales for x and y
				//scale(entity, (float) (1 / size.y));
				Quaternion srcRot = raytraceRotation();
				Quaternion dstRot = target.raytraceRotation();
				Vec3 srcOff = raytraceOffset();
				Vec3 dstOff = target.raytraceOffset();
				
				Vec3 oldPos = new Vec3(entity.xOld, entity.yOld, entity.zOld);
				Vec3 oPos = new Vec3(entity.xo, entity.yo, entity.zo);
				Vec3 pos = position;
				if (target != this) {
					oldPos = VecMath.transform(oldPos, srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, srcOff, dstOff);
					oPos = VecMath.transform(oPos, srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, srcOff, dstOff);
					pos = VecMath.transform(pos, srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, srcOff, dstOff);
				}
				
				Vec2 rotVec = VecMath.lookAngle(VecMath.transform(VecMath.getLookVec(entity.getRotationVector()), srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), this == target, Vec3.ZERO, Vec3.ZERO));
				Vec2 oldRotVec = VecMath.lookAngle(VecMath.transform(VecMath.getLookVec(entity.getRotationVector()), srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), this == target, Vec3.ZERO, Vec3.ZERO));
				
				entity.setXRot(rotVec.x);
				entity.xRotO = oldRotVec.x;
				entity.setYRot(rotVec.y);
				entity.yRotO = oldRotVec.y;
				
				motion = VecMath.transform(motion, srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), target == this, Vec3.ZERO, Vec3.ZERO);
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
	
	@Override
	public boolean moveParticle(ParticleAccessor particle, Vec3 position, Vec3 motion) {
		boolean wasInFront = isInFront(position);
		boolean isInFront = isInFront(position.add(motion));
		if (wasInFront != isInFront) {
//			// TODO: do stuff with this
//			Vec3 rot = VecMath.toDeegrees(rotation);
//			if ((((int) (rot.x * 3)) / 3) % 90 == 0 && (((int) (rot.y * 3)) / 3) % 90 == 0 && (((int) (rot.z * 3)) / 3) % 90 == 0) {
//				double x = RaytraceHelper.calculateXOffset(box, entity.getBoundingBox(), motion.x);
//				double y = RaytraceHelper.calculateYOffset(box, entity.getBoundingBox(), motion.y);
//				double z = RaytraceHelper.calculateZOffset(box, entity.getBoundingBox(), motion.z);
//				if (x != motion.x || y != motion.y || z != motion.z)
//					System.out.println(raytraceOffset() + "multiple");
//			}
			
			double raytraceDistance = trace(position, position.add(motion));
			// luigi: not sure if this comparison between raytrace distance and distance from entity to portal is a good idea or not
			double distanceEntityToPortal = position.distanceTo(portalQuad.center().add(raytraceOffset()));
			if (raytraceDistance != -1 && distanceEntityToPortal < raytraceDistance || overlaps(particle.getBBox()) || overlaps(particle.getBBox().move(motion))) {
				// Luigi's TODO: individual scales for x and y
				//scale(entity, (float) (1 / size.y));
				Quaternion srcRot = raytraceRotation();
				Quaternion dstRot = target.raytraceRotation();
				Vec3 srcOff = raytraceOffset();
				Vec3 dstOff = target.raytraceOffset();
				
				Vec3 oldPos = particle.getOldPosition();
				Vec3 oPos = particle.getPosition();
				Vec3 pos = position;
				if (target != this) {
					oldPos = VecMath.transform(oldPos, srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, srcOff, dstOff);
					oPos = VecMath.transform(oPos, srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, srcOff, dstOff);
					pos = VecMath.transform(pos, srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), false, srcOff, dstOff);
				}
				
				motion = VecMath.transform(motion, srcRot, dstRot, getScaleRatio(), target.get180DegreesRotationAroundVerticalAxis(), target == this, Vec3.ZERO, Vec3.ZERO);
				particle.move(motion);
				particle.setPosition(pos.x, pos.y, pos.z);
				particle.move(motion);
				
				particle.setPosition(oPos.x, oPos.y, oPos.z);
				particle.setOldPosition(oldPos.x, oldPos.y, oldPos.z);
				
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void tickChunkTracking(Player player) {
		// TODO: do level properly, maybe?
		List<SectionPos> positions = TrackyTools.getChunksForPortal(player.level, player, this);
		SectionPos center = SectionPos.of(new BlockPos(target.raytraceOffset().x, target.raytraceOffset().y, target.raytraceOffset().z));
		// TODO: optimize
		// TODO: don't redundantly do this
		// TODO: offset this to be centered around the translated player camera
		// TODO: frontface cull this to be only portals on the opposite side of the portal than the player's on
		List<SectionPos> current = new ArrayList<>();
		
		for (int x = -8; x <= 8; x++) {
			for (int y = -8; y <= 8; y++) {
				for (int z = -8; z <= 8; z++) {
					SectionPos ps = SectionPos.of(center.getX() + x, center.getY() + y, center.getZ() + z);
					boolean pz = positions.remove(ps);
					if (!player.level.isClientSide)
						if (!pz) TrackyAccessor.markForRetracking(player);
					current.add(ps);
				}
			}
		}

//		if (!player.level.isClientSide)
//			if (!positions.isEmpty())
//				TrackyAccessor.markForRetracking(player);
		
		positions.clear();
		positions.addAll(current);
	}
	
	@Override
	public Vec3 nearestPoint(Vec3 targetSpot) {
		targetSpot = targetSpot.subtract(position.x, position.y, position.z);
		return portalQuad.nearest(targetSpot).add(position.x, position.y, position.z);
	}
	
	public CompoundTag serialize() {
//		protected Vector3d position;
//		protected Vec2d size;
//		protected Vec3 rotation;
//		protected Vec3 renderNormal;
//		protected Vec3 computedNormal;
//		protected Quad portalQuad;
//		AABB box = null;
		
		CompoundTag tag = new CompoundTag();
		tag.putUUID("UUID", uuid);
		tag.putUUID("TargetUUID", target.uuid);
		tag.put("Position", toList(position.x, position.y, position.z));
		tag.put("Size", toList(size.x, size.y));
		tag.put("Rotation", toList(rotation.x, rotation.y, rotation.z));
		if (renderNormal != null)
			tag.put("RenderNormal", toList(renderNormal.x, renderNormal.y, renderNormal.z));
		tag = writeAdditional(tag);
		return tag;
	}
	
	protected static ListTag toList(double... values) {
		ListTag tag = new ListTag();
		for (double value : values) tag.add(DoubleTag.valueOf(value));
		return tag;
	}
	
	public CompoundTag writeAdditional(CompoundTag tag) {
		return tag;
	}
}
