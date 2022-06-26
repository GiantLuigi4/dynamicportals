package tfc.dynamicportals.util;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
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

	public static double getQuaternionMagnitude(Quaternion q) {
		return Mth.fastInvSqrt(q.r() * q.r() + q.i() * q.i() + q.j() * q.j() + q.k() * q.k());
	}

	public static Vec3 old_transform(Vec3 src, Quaternion selfRotation, Quaternion otherRotation, boolean isMirror, boolean motion, Vec3 sourceTransformation, Vec3 destTransformation) {
		if (motion) {
			Quaternion selfRotConj = selfRotation.copy(); selfRotConj.conj();
			Quaternion otherRotConj = otherRotation.copy(); otherRotConj.conj();

			Vec3 pos = VecMath.rotate(src, selfRotConj);
			if (isMirror) {
				pos = pos.multiply(1, 1, -1);
			}
			pos = VecMath.rotate(pos, otherRotation);
			pos = VecMath.rotate(pos, new Quaternion(0, 1, 0, 0));
			return pos;
		}
		Vec3 pos = src.subtract(sourceTransformation);
		pos = old_transform(pos, selfRotation, otherRotation, isMirror, true, sourceTransformation, destTransformation);
		pos = pos.add(destTransformation);
		return pos;
	}

	public static Vec3 transform(Vec3 src, Quaternion selfRotation, Quaternion otherRotation, boolean isMirror, boolean motion, Vec3 sourceTransformation, Vec3 destTransformation) {
		if (motion) {
			Quaternion selfRot = selfRotation.copy();
			Quaternion otherRot = otherRotation.copy();

			Vector3f selfRotationVec = selfRot.toYXZ();
			Vector3f otherRotVec = otherRot.toYXZ();

			Vec3 pos = src
					.yRot(-otherRotVec.y())
					.yRot(selfRotationVec.y())
					.yRot((float) Math.PI);

			return pos;
		}
		Vec3 pos = src.subtract(sourceTransformation);
		pos = transform(pos, selfRotation, otherRotation, isMirror, true, sourceTransformation, destTransformation);
		pos = pos.add(destTransformation);
		return pos;
	}
}
