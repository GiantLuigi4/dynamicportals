package tfc.dynamicportals.api;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.util.Vec2d;
import tfc.dynamicportals.util.VecMath;

import java.util.UUID;

/**
 * the base class for all portals
 * {@link BasicPortal} for some examples
 */
public abstract class AbstractPortal {
	public final UUID uuid;
	public AbstractPortal target = this;
	public PortalRenderer renderer;
	
	public AbstractPortal(UUID uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * sets the target portal, thus creating a linked pair
	 * if a portal is linked to itself, it should be a mirror
	 *
	 * @param target the other portal in the pair
	 */
	public void setTarget(AbstractPortal target) {
		this.target = target;
	}
	
	/**
	 * @return the offset for the raytrace vector
	 * this should generally be the portal's position
	 */
	public abstract Vec3 raytraceOffset();
	
	/**
	 * @return the quaternion for rotating the look vector for raytracing
	 * this should generally be the opposite of the portal's rotation
	 */
	public abstract Quaternion raytraceRotation();
	
	/**
	 * @return ratio between target size and this size
	 */
	public Vec3 getScaleRatio() {
		return new Vec3(1, 1, 1);
	}
	
	/**
	 * @return portal size vector
	 */
	public abstract Vec2d getSize();
	
	/**
	 * @return quaternion defining the actual rotation of the portal
	 */
	public Quaternion getActualRotation() {
		Quaternion quaternion = raytraceRotation();
		if (target == this) quaternion.mul(new Quaternion(0, -90, 0, true));
		quaternion.conj();
		return quaternion;
	}
	
	/**
	 * @return a quaternion for rotating the look vector by 180 degrees around the vertical axis of the portal
	 * for now used only by startVec and reachVec in the raytracing, maybe should be used for everything
	 * (position vector for motion as well)
	 */
	public Quaternion get180DegreesRotationAroundVerticalAxis() {
		Quaternion rot = Quaternion.ONE.copy();
		Quaternion q = this.raytraceRotation();
		q.conj();
		rot.mul(new Vector3f(VecMath.rotate(new Vec3(0, 1, 0), q)).rotationDegrees(180));
		return rot;
	}
	
	/**
	 * raytraces between the start vec and the portal
	 *
	 * @param start the start vector
	 * @param end   the end vector
	 * @return the distance between the start vec and the portal
	 * if it does not hit the portal, it should return 1
	 */
	public abstract double trace(Vec3 start, Vec3 end);
	
	/**
	 * checks if the entity's bounding box is overlapping the portal
	 * in the case of {@link BasicPortal}, it checks if the bounding box overlaps the portal's quad
	 *
	 * @param box the bounding box
	 * @return if it overlaps the portal
	 */
	public abstract boolean overlaps(AABB box);
	
	/**
	 * used for checking if the entity has crossed through the portal for sake of teleportation
	 * if the entity is infront before being moved, and not infront after, then it will call {@link #moveEntity(Entity, Vec3, Vec3)}
	 *
	 * @param entity   the entity in question
	 * @param position the entity's position
	 * @return if the entity is currently infront of the portal with the given position vector
	 */
	public boolean isInFront(Entity entity, Vec3 position) {
		return true;
	}
	
	/**
	 * here is where you handle teleporting an entity
	 *
	 * @param entity   the entity to teleport
	 * @param position the entity's position before teleporting
	 * @param motion   the entity's current motion vector
	 * @return whether or not the entity was moved
	 */
	public boolean moveEntity(Entity entity, Vec3 position, Vec3 motion) {
		return false;
	}
	
	public void finishMove(Entity entity, Vec3 position, Vec3 motion) {
	}
	
	public abstract void tickChunkTracking(Player player);
}
