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

	public static Vec3 rotate(Vec3 src, Quaternion rotation) {
		Quaternion point = new Quaternion((float) src.x, (float) src.y, (float) src.z, 0);

		Quaternion newPoint = rotation.copy();

		// https://danceswithcode.net/engineeringnotes/quaternions/quaternions.html
		// We take the conj before because we're doing an ACTIVE rotation
		// (point rotated around the plane) not a PASSIVE one (plane rotated around the point)
		// ACTIVELY ROTATED p' = q^ * p * q (q^ is inverse, since magnitude is 1, conjugate==inverse)

		newPoint.conj(); //convert q to q^
		point.mul(newPoint); //pre-multiplication q^ * p
		newPoint.conj(); //revert q^ to q
		newPoint.mul(point); //post-multiplication p * q

		// I have no idea if I explained it well, it's already hard for me to grasp it
		// God bless who created that website
		return new Vec3(newPoint.i(), newPoint.j(), newPoint.k());
	}

	public static double getQuaternionMagnitude(Quaternion q) {
		return Mth.fastInvSqrt(q.r() * q.r() + q.i() * q.i() + q.j() * q.j() + q.k() * q.k());
	}

	public static Vec3 old_transform(Vec3 src, Quaternion selfRotation, Quaternion otherRotation, boolean isMirror, boolean motion, Vec3 sourceTransformation, Vec3 destTransformation) {
		if (motion) {
			Quaternion selfRotConj = selfRotation.copy();
			selfRotConj.conj();

			//Luigi, in your previous code you were doing this:
			//  rotate by selfConj, rotate by 180, rotate by self, rotate by self, rotate by other
			//Now, we can rotate by 180 whenever you want, since it just flips the signs of x and y
			//If we move this rotation at the end, we have:
			//  rotate by selfConj, rotate by self, rotate by self, rotate by other, rotate by 180
			//But wait! We rotate by selfConj and then self: they nullify each other. So we are just doing:
			//  rotate by self, rotate by other, rotate by 180
			//Which is exactly what I do, but the rotations are reversed
			//But in theory quaternion multiplication is almost never commutative, so why is it now??
			//Maybe because the rotation happens around the Y axis?


			Quaternion otherRotConj = otherRotation.copy(); otherRotConj.conj();
			Quaternion selfRot = selfRotation.copy();
			selfRot.mul(otherRotConj);
			Vec3 pos = VecMath.rotate(src, selfRot);
			System.out.println(src + "; " + selfRot.toYXZDegrees() + "; " + pos);
//			Vec3 pos = VecMath.rotate(src, otherRotConj);
//			pos = VecMath.rotate(pos, selfRotation);

//			pos = VecMath.rotate(pos, selfRotation);
//			pos = VecMath.rotate(pos, otherRotConj);

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
