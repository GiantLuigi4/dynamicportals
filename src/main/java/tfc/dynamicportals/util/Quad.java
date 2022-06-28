package tfc.dynamicportals.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Quad {
	// bottom left, bottom right, top right, top left
	public final Vec3 pt0, pt1, pt2, pt3;
	
	public Quad(Vec3 pt0, Vec3 pt1, Vec3 pt2, Vec3 pt3) {
		this.pt0 = pt0;
		this.pt1 = pt1;
		this.pt2 = pt2;
		this.pt3 = pt3;
	}
	
	private static double bulkMin(double... values) {
		double out = values[0];
		for (int i = 1; i < values.length; i++) out = Math.min(out, values[i]);
		return out;
	}
	
	private static boolean bulkEq(double... values) {
		double first = values[0];
		for (int i = 1; i < values.length; i++) if (values[i] != first) return false;
		return true;
	}
	
	private static double bulkMax(double... values) {
		double out = values[0];
		for (int i = 1; i < values.length; i++) out = Math.min(out, values[i]);
		return out;
	}
	
	public boolean voxelOverlap(AABB box) {
		if (bulkEq(pt0.y, pt1.y, pt2.y, pt3.y)) {
			// TODO: get the main overlap check to work better
			return new AABB(
					bulkMin(pt0.x, pt1.x, pt2.x, pt3.x),
					pt0.y,
					bulkMin(pt0.z, pt1.z, pt2.z, pt3.z),
					bulkMax(pt0.x, pt1.x, pt2.x, pt3.x),
					pt0.y,
					bulkMax(pt0.z, pt1.z, pt2.z, pt3.z)
			).intersects(box);
		}
		return false;
	}
	
	// TODO: this works well enough for now, but it's bad and dumb
	public boolean overlaps(AABB box) {
		// TODO: this is a temp fix
		// it is bad
		if (voxelOverlap(box)) {
			return true;
		}
		return getNearest(box.getCenter()) != null;
	}
	
	double absMax(double d0, double d1) {
		if (Math.abs(d0) > Math.abs(d1)) return d0;
		return d1;
	}
	
	boolean absGreatest(double v0, double v1, double v2) {
		v0 = Math.abs(v0);
		if (v0 > Math.abs(v1) && v0 > Math.abs(v2)) return true;
		return false;
	}
	
	public Vec3 getNearest(Vec3 center) {
		double dx_ = absMax(pt0.x - pt1.x, pt3.x - pt2.x);
		double dy_ = absMax(pt3.y - pt0.y, pt2.y - pt1.y);
		double dz_ = absMax(pt1.z - pt0.z, pt2.z - pt3.z);
		
		if (absGreatest(dz_, dx_, dy_)) {
			return null;
		} else if (absGreatest(dx_, dy_, dz_)) {
			// TODO: check all of this
			double dx0 = center.x / (pt0.x - pt1.x);
			dx0 += 0.5;
			if (dx0 < 0 || dx0 > 1) return null;
			Vec3 interpTop = VecMath.lerp(dx0, pt1, pt0);
			
			dx0 = center.x / (pt3.x - pt2.x);
			dx0 += 0.5;
			if (dx0 < 0 || dx0 > 1) return null;
			Vec3 interpBottom = VecMath.lerp(dx0, pt2, pt3);
			
			double y = interpTop.y - interpBottom.y;
			double z = interpTop.z - interpBottom.z;
			if (Math.abs(y) > Math.abs(z)) {
				// TODO: check
				double tx = center.y + y;
				double dz0 = Math.abs(tx) / y;
				if (tx < 0) dz0 = -dz0;
				dz0 = 1 - dz0;
				if (0 > dz0 || dz0 > 1) return null;
				Vec3 point = VecMath.lerp(dz0, interpTop, interpBottom);
				return point;
			} else {
				double tx = center.z + z;
				double dz0 = Math.abs(tx) / z;
				if (tx < 0) dz0 = -dz0;
				dz0 = 1 - dz0;
				if (0 > dz0 || dz0 > 1) return null;
				Vec3 point = VecMath.lerp(dz0, interpTop, interpBottom);
				return point;
			}
		}
		// TODO: deal with quads that have no height
		double dy0 = center.y / (pt3.y - pt0.y);
		if (dy0 < 0 || dy0 > 1) return null;
		Vec3 interpLeft = VecMath.lerp(dy0, pt0, pt3);
		
		dy0 = center.y / (pt2.y - pt1.y);
		if (dy0 < 0 || dy0 > 1) return null;
		Vec3 interpRight = VecMath.lerp(dy0, pt1, pt2);
		
		double x = interpLeft.x - interpRight.x;
		double z = interpLeft.z - interpRight.z;
		if (Math.abs(x) > Math.abs(z)) {
			double tx = center.x + (x / 2);
			double dx0 = Math.abs(tx) / x;
			if (tx < 0) dx0 = (-dx0);
			if (0 > dx0 || dx0 > 1) return null;
			Vec3 point = VecMath.lerp(dx0, interpRight, interpLeft);
			return point;
		} else {
			double tx = center.z + (z / 2);
			double dx0 = Math.abs(tx) / z;
			if (tx < 0) dx0 = (-dx0);
			if (0 > dx0 || dx0 > 1) return null;
			Vec3 point = VecMath.lerp(dx0, interpRight, interpLeft);
			return point;
		}
	}
}
