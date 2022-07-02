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
		for (int i = 1; i < values.length; i++) out = Math.max(out, values[i]);
		return out;
	}
	
	public boolean overlaps(AABB box) {
		Vec3 P = box.getCenter();
		Vec3 n = nearestInQuad(P);
		return n != null && box.contains(n);
	}
	
	public Vec3 nearest(Vec3 point) {
		Vec3 n;
		return (n = nearestInQuad(point)) == null ? nearestOnEdge(point) : n;
	}
	
	public Vec3 nearestInQuad(Vec3 P) {
		Vec3[] poly = new Vec3[]{pt0, pt1, pt2, pt3};
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
		A.multiply(1 / det);
		
		Vec3 nearest = new Vec3(
				A.m00 * P.x + A.m01 * P.y + A.m02 * P.z + A.m03 * d,
				A.m10 * P.x + A.m11 * P.y + A.m12 * P.z + A.m13 * d,
				A.m20 * P.x + A.m21 * P.y + A.m22 * P.z + A.m33 * d
		);
		int i, j;
		boolean inside = false;
		for (i = 0, j = poly.length - 1; i < poly.length; j = i++) {
			if ((((poly[i].x <= nearest.x) && (nearest.x < poly[j].x)) |
					((poly[j].x <= nearest.x) && (nearest.x < poly[i].x))) &&
					(nearest.y < (poly[j].y - poly[i].y) * (nearest.x - poly[i].x) / (poly[j].x - poly[i].x) + poly[i].y))
				inside = !inside;
		}
		
		return inside ? nearest : null;
	}
	
	public Vec3 nearestOnEdge(Vec3 P) {
		Vec3[] vert = new Vec3[]{pt0, pt1, pt2, pt3};
		double minDistance = Double.POSITIVE_INFINITY;
		Vec3 actualNearest = null;
		for (int i = 0; i < 4; i++) {
			Vec3 possibleNearest = nearestOnEdgeAB(vert[i], vert[(i + 1) % 4], P);
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
		double t = -(v.dot(u) / v.dot(v));
		if (t < 0 || t > 1) return null;
		return A.scale(1 - t).add(B.scale(t));
	}
	
	public Vec3 center() {
		Vec3 bMin = new Vec3(
				bulkMin(pt0.x, pt1.x, pt2.x, pt3.x),
				bulkMin(pt0.y, pt1.y, pt2.y, pt3.y),
				bulkMin(pt0.z, pt1.z, pt2.z, pt3.z)
		);
		Vec3 bMax = new Vec3(
				bulkMax(pt0.x, pt1.x, pt2.x, pt3.x),
				bulkMax(pt0.y, pt1.y, pt2.y, pt3.y),
				bulkMax(pt0.z, pt1.z, pt2.z, pt3.z)
		);
		Vec3 mid = bMin.add(bMax).scale(0.5);
		return mid;
	}
}
