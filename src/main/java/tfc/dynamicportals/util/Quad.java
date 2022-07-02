package tfc.dynamicportals.util;

import com.mojang.math.Matrix4f;
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
		Vec3 P = box.getCenter();
		Vec3 n = nearestInQuad(P);
		return n != null && box.contains(n);
	}
	
	double absMax(double d0, double d1) {
		if (Math.abs(d0) > Math.abs(d1)) return d0;
		return d1;
	}
	
	boolean absGreatest(double v0, double v1, double v2) {
		v0 = Math.abs(v0);
		return v0 > Math.abs(v1) && v0 > Math.abs(v2);
	}
	
	public Vec3 nearestInQuad(Vec3 P) {
		Vec3[] vert = new Vec3[]{pt0, pt1, pt2, pt3};
		Vec3 normal = (pt0.subtract(pt1)).cross(pt2.subtract(pt1));
		Vec3 n = normal.multiply(pt0).scale(-1);
		double d = (n.x + n.y + n.z);
		Matrix4f A = new Matrix4f(new float[]{
				1, 0, 0, (float) -normal.x,
				0, 1, 0, (float) -normal.y,
				0, 0, 1, (float) -normal.z,
				(float) normal.x, (float) normal.y, (float) normal.z, 0,
		});
		float det = A.determinant();
		A.adjugateAndDet();
		A.multiply(1/det);
		
		Vec3 nearest = new Vec3(
				A.m00*P.x+A.m01*P.y+A.m02*P.z+A.m03*d,
				A.m10*P.x+A.m11*P.y+A.m12*P.z+A.m13*d,
				A.m20*P.x+A.m21*P.y+A.m22*P.z+A.m33*d
		);
//
//		boolean inside = false;
//		for (int i = 0, j = vert.length - 1; i<vert.length; j=i++) {
//			double xi = vert[i].x, yi = vert[i].y, zi = vert[i].z;
//			double xj = vert[j].x, yj = vert[j].y, zj = vert[j].z;
//
//			boolean intersect = ((yi > nearest.y) != (yj > nearest.y))
//					&& (nearest.x < (xj-xi)*(nearest.y-yi)/(yj -yi)+xi);
//			if (intersect) inside = !inside;
//		}
		boolean inside =
				Math.abs(nearest.x) <= Math.abs(pt0.x - pt1.x) / 2 &&
						Math.abs(nearest.y) <= Math.abs(pt0.y - pt2.y) &&
						Math.abs(nearest.z) <= Math.abs(pt0.z - pt1.z) / 2
				
				;
		
		
		return inside ? nearest : null;
	}
	
	public Vec3 nearestOnEdge(Vec3 P) {
		Vec3[] vert = new Vec3[]{pt0, pt1, pt2, pt3};
		double minDistance = Double.POSITIVE_INFINITY;
		Vec3 actualNearest = null;
		for (int i = 0; i < 4; i++) {
			Vec3 possibleNearest = nearestOnEdgeAB(vert[i], vert[(i+1)%4], P);
			if (possibleNearest != null) {
				double possibleDistance = P.distanceToSqr(possibleNearest);
				if (minDistance > possibleDistance) {
					actualNearest = possibleNearest;
					minDistance = possibleDistance;
				}
			}
		}
		return actualNearest;
	}
	
	public Vec3 nearestOnEdgeAB(Vec3 A, Vec3 B, Vec3 P) {
		Vec3 v = B.subtract(A);
		Vec3 u = A.subtract(P);
		double t = - (v.dot(u) / v.dot(v));
		if (t < 0 || t > 1) return null;
		return A.scale(1 - t).add(B.scale(t));
	}
}
