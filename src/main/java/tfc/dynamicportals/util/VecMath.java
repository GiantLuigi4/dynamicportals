package tfc.dynamicportals.util;

import com.mojang.math.Quaternion;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class VecMath {
	public static Vec3 lerp(double pct, Vec3 start, Vec3 end) {
		return new Vec3(
				Mth.lerp(pct, start.x, end.x),
				Mth.lerp(pct, start.y, end.y),
				Mth.lerp(pct, start.z, end.z)
		);
	}
	
	// I don't even know why I tried to fix something that was already correct
	// https://danceswithcode.net/engineeringnotes/quaternions/quaternions.html
	public static Vec3 rotate(Vec3 src, Quaternion rotation) {
		Quaternion point = new Quaternion((float) src.x, (float) src.y, (float) src.z, 0);
		Quaternion newPoint = rotation.copy();
		point.mul(newPoint);
		newPoint.conj();
		newPoint.mul(point);
		return new Vec3(newPoint.i(), newPoint.j(), newPoint.k());
	}
	
	public static Vec3 old_transform(Vec3 src, Quaternion selfRot, Quaternion otherRot, boolean isMirror, boolean motion, Vec3 sourceTransformation, Vec3 destTransformation) {
		if (motion) {
			Quaternion selfRotConj = selfRot.copy();
			selfRotConj.conj();
			Quaternion otherRotConj = otherRot.copy();
			otherRotConj.conj();
			
			Vec3 pos = VecMath.rotate(src, selfRot);
			if (isMirror) {
				pos = pos.multiply(1, 1, -1);
			}
			pos = VecMath.rotate(pos, otherRotConj);
			return pos;
		}
		Vec3 pos = src.subtract(sourceTransformation);
		pos = old_transform(pos, selfRot, otherRot, isMirror, true, sourceTransformation, destTransformation);
		pos = pos.add(destTransformation);
		return pos;
	}
}
