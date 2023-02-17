package tfc.dynamicportals.access;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface ParticleAccessor {
	Vec3 getPosition();
	Vec3 getOldPosition();
	Vec3 getMotion();
	void setPosition(double x, double y, double z);
	void setOldPosition(double x, double y, double z);
	void move(Vec3 motion);
	AABB getBBox();
}
