package tfc.dynamicportals.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Quad {
	// bottom left, bottom right, top right, top left
	Vec3 pt0, pt1, pt2, pt3;
	
	public Quad(Vec3 pt0, Vec3 pt1, Vec3 pt2, Vec3 pt3) {
		this.pt0 = pt0;
		this.pt1 = pt1;
		this.pt2 = pt2;
		this.pt3 = pt3;
	}
	
	// TODO: this works well enough for now, but it's bad and dumb
	public boolean overlaps(AABB box) {
		// TODO: deal with quads that have no height
		double dy0 = box.getCenter().y / (pt3.y - pt0.y);
		if (dy0 < 0 || dy0 > 1) return false;
		Vec3 interpLeft = VecMath.lerp(dy0, pt0, pt3);
		
		dy0 = box.getCenter().y / (pt2.y - pt1.y);
		if (dy0 < 0 || dy0 > 1) return false;
		Vec3 interpRight = VecMath.lerp(dy0, pt1, pt2);
		
		double x = interpLeft.x - interpRight.x;
		double z = interpLeft.z - interpRight.z;
		if (Math.abs(x) > z) {
			double dx0 = Math.abs(box.getCenter().x + (x / 2)) / x;
			if (dx0 < -1 || dx0 > 1) return false;
//			Vec3 point = VecMath.lerp(dx0, interpRight, interpLeft);
//			return box.contains(point);
			return true;
		} else {
			double dx0 = Math.abs(box.getCenter().z + (z / 2)) / z;
			if (dx0 < -1 || dx0 > 1) return false;
//			Vec3 point = VecMath.lerp(dx0, interpRight, interpLeft);
//			return box.contains(point);
			return true;
		}
	}
}
