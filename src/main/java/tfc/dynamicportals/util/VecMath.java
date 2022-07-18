package tfc.dynamicportals.util;

import com.mojang.math.Quaternion;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class VecMath {
	
	public static Vec3 lerp(double pct, Vec3 start, Vec3 end) {
//		return new Vec3(
//					Mth.lerp(pct, start.x, end.x),
//					Mth.lerp(pct, start.y, end.y),
//					Mth.lerp(pct, start.z, end.z)
//			);
		return start.add(end.subtract(start).scale(pct));
	}
	
	public static Vec3 toRadians(Vec3 src) {
		return new Vec3(Math.toRadians(src.x), Math.toRadians(src.y), Math.toRadians(src.z));
	}
	
	public static Vec3 toDeegrees(Vec3 src) {
		return new Vec3(Math.toDegrees(src.x), Math.toDegrees(src.y), Math.toDegrees(src.z));
	}
	
	public static Vec3 rotate(Vec3 src, Quaternion rotation) {
		Quaternion point = new Quaternion((float) src.x, (float) src.y, (float) src.z, 0);
		Quaternion newPoint = rotation.copy();
		point.mul(newPoint);
		newPoint.conj();
		newPoint.mul(point);
		return new Vec3(newPoint.i(), newPoint.j(), newPoint.k());
	}
	
	public static Vec3 transform(Vec3 src, Quaternion selfRot, Quaternion otherRot, Vec3 scaleVec, Quaternion aroundYAxisRot, boolean isMirror, Vec3 sourceTransformation, Vec3 otherTransformation) {
		if (sourceTransformation.equals(Vec3.ZERO) && otherTransformation.equals(Vec3.ZERO)) {
			Quaternion otherRotConj = otherRot.copy();
			otherRotConj.conj();
			
			Vec3 pos = VecMath.rotate(src, selfRot);
			if (isMirror) {
				pos = pos.multiply(1, 1, -1);
			}
			pos = VecMath.rotate(pos, otherRotConj);
			pos = pos.multiply(scaleVec);
			pos = VecMath.rotate(pos, aroundYAxisRot);
			return pos;
		}
		return transform(src.subtract(sourceTransformation), selfRot, otherRot, scaleVec, aroundYAxisRot, isMirror, Vec3.ZERO, Vec3.ZERO).add(otherTransformation);
	}
	
	public static Vec3 getLookVec(Vec2 vec) {
		float f = vec.x * ((float) Math.PI / 180F);
		float f1 = -vec.y * ((float) Math.PI / 180F);
		float f2 = Mth.cos(f1);
		float f3 = Mth.sin(f1);
		float f4 = Mth.cos(f);
		float f5 = Mth.sin(f);
		return new Vec3(f3 * f4, -f5, f2 * f4);
	}
	
	public static Vec2 lookAngle(Vec3 vector) {
		double d0 = vector.x - 0;
		double d1 = vector.y - 0;
		double d2 = vector.z - 0;
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		double xr = (Mth.wrapDegrees((float) (-(Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)))));
		double yr = (Mth.wrapDegrees((float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F));
		return new Vec2((float) xr, (float) yr);
	}
}
