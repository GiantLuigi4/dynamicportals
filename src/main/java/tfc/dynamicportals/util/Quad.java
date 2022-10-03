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
	
	public Vec3 nearestInQuad(Vec3 point) {
		Vec3[] vertices = new Vec3[]{pt0, pt1, pt2, pt3};
		
		//The equation of a 3d plane is of the type ax+by+cz+d=0
		
		//The normal vector of this plane is (a,b,c)
		//We calculate it by doing the cross product of two of the edges:
		//Given two vectors, the third perpendicular to both of them is one and only one
		Vec3 normal = normal();
		//d represents the offset from the origin, since there is none we assume it's 0
		
		//Consider the matrix equation Ax=B, x=B/A=inv(A)*B
		//"A" is the matrix of our plane, "B" is a 1x4 matrix containing (point.x,point.y,point.z,d=0)
		Matrix4f A = new Matrix4f(new float[]{
				1, 0, 0, (float) -normal.x,
				0, 1, 0, (float) -normal.y,
				0, 0, 1, (float) -normal.z,
				(float) normal.x, (float) normal.y, (float) normal.z, 0,
		});
		
		//The inverse of a matrix A is its adjugated (idk what it is) times the inverse of its det
		float det = A.determinant();
		A.adjugateAndDet();
		A.multiply(1 / det);
		
		//Matrix multiplication between inv(A) and B
		Vec3 nearest = new Vec3(
				A.m00 * point.x + A.m01 * point.y + A.m02 * point.z,
				A.m10 * point.x + A.m11 * point.y + A.m12 * point.z,
				A.m20 * point.x + A.m21 * point.y + A.m22 * point.z
		);
		
		//https://stackoverflow.com/questions/62475889/point-in-polygon-3d-same-plane-algorithm
		boolean inside = true;
		Vec3 d0 = (nearest.subtract(pt0)).cross(pt0.subtract(pt3));
		for (int i = 1; i < vertices.length; i++) {
			Vec3 di = nearest.subtract(vertices[i]).cross(vertices[i].subtract(vertices[i - 1]));
			if (d0.dot(di) <= 0) {
				inside = false;
				break;
			}
		}
		return inside ? nearest : null;
	}
	
	public Vec3 nearestOnEdge(Vec3 point) {
		Vec3[] vert = new Vec3[]{pt0, pt1, pt2, pt3};
		double minDistance = Double.POSITIVE_INFINITY;
		Vec3 actualNearest = null;
		for (int i = 0; i < 4; i++) {
			Vec3 possibleNearest = nearestOnEdgeAB(vert[i], vert[(i + 1) % 4], point);
			if (possibleNearest != null) {
				double possibleDistance = point.distanceToSqr(possibleNearest);
				if (minDistance > possibleDistance) {
					actualNearest = possibleNearest;
					minDistance = possibleDistance;
				}
			}
		}
		// luigi: the code above doesn't account for corners, so I've strapped this on
		if (actualNearest == null) {
			for (Vec3 vec3 : vert) {
				double d = vec3.distanceTo(point);
				if (d < minDistance) {
					minDistance = d;
					actualNearest = vec3;
				}
			}
		}
		return actualNearest;
	}
	
	public Vec3 nearestOnEdgeAB(Vec3 A, Vec3 B, Vec3 point) {
		Vec3 v = B.subtract(A);
		Vec3 u = A.subtract(point);
		double t = -(v.dot(u) / v.dot(v));
		if (t < 0 || t > 1) return null;
		return VecMath.lerp(t, A, B);
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
		return bMin.add(bMax).scale(0.5);
	}
	
	public Vec3 normal() {
		return (pt1.subtract(pt0)).cross(pt2.subtract(pt1)).normalize();
	}
}
