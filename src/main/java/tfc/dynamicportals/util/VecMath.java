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

	public static Vec3 rotate(Vec3 v, Quaternion rotation) {
		Vec3 u = new Vec3(rotation.i(), rotation.j(), rotation.k());
		float s = rotation.r();

		return (u.scale(u.dot(v)).scale(2)).add(v.scale(s*s-u.dot(v))).add(u.cross(v).scale(s).scale(2));
//
//		double q0 = rotation.r(), q1 = rotation.i(), q2 = rotation.j(), q3 = rotation.k();
//		double x = (1 - 2 * q2 * q2 - 2 * q3 * q3) * v.x + 2 * (q1 * q2 + q0 * q3) * v.y + 2 * (q1 * q3 - q0 * q2) * v.z;
//		double y = 2 * (q1 * q2 - q0 * q3) * v.x + (1 - 2 * q1 * q1 - 2 * q3 * q3) * v.y + 2 * (q2 * q3 + q0 * q1) * v.z;
//		double z = 2 * (q1 * q3 + q0 * q2) * v.x + 2 * (q2 * q3 - q0 * q1) * v.y + (1 - 2 * q1 * q1 - 2 * q2 * q2) * v.z;
//		return new Vec3(x, y, z);

//		Quaternion p = new Quaternion((float) v.x, (float) v.y, (float) v.z, 0);
//		Quaternion q = rotation.copy();
//		p.mul(q);
//		q.conj();
//		q.mul(p);
//		return new Vec3(p.i(), p.j(), p.k());

	}

	public static Vec3 old_transform(Vec3 src, Quaternion selfRotation, Quaternion otherRotation, boolean isMirror, boolean motion, Vec3 sourceTransformation, Vec3 destTransformation) {
		if (motion) {
			Vec3 pos = src;

			// TODO: this doesn't work properly (it actually kinda does)
			Quaternion conj = selfRotation.copy();
			conj.conj();
			pos = VecMath.rotate(pos, conj);
			pos = pos.multiply(-1, 1, -1);
			pos = VecMath.rotate(pos, selfRotation);
			pos = VecMath.rotate(pos, selfRotation);
			Quaternion otherConj = otherRotation.copy();
			otherConj.conj();
			pos = VecMath.rotate(pos, otherConj);

			return pos;
		}
		Vec3 pos = src.subtract(sourceTransformation);
		pos = old_transform(pos, selfRotation, otherRotation, isMirror, true, sourceTransformation, destTransformation);
		pos = pos.add(destTransformation);
		return pos;
	}

	public static Vec3 start_transform(Vec3 src, Quaternion selfRotation, Quaternion otherRotation, boolean isMirror, boolean motion, Vec3 sourceTransformation, Vec3 destTransformation) {
		if (motion) {
			Vec3 pos = src;
			Quaternion selfRotConj = selfRotation.copy();
			selfRotConj.conj();
			Quaternion otherRotConj = otherRotation.copy();
			otherRotConj.conj();

			Vector3f selfRotationVec = selfRotConj.toYXZ();
			Vector3f otherRotVec = otherRotConj.toYXZ();

			//So, if one rotation is 0, rotate by otherConj and then by self

			pos = pos
//					.yRot(selfRotationVec.y())
					.yRot(otherRotVec.y())
					.yRot(-selfRotationVec.y())
			;
			pos = pos.yRot((float) Math.PI); //idk...


			return pos;
		}
		Vec3 pos = src.subtract(sourceTransformation);
		pos = start_transform(pos, selfRotation, otherRotation, isMirror, true, sourceTransformation, destTransformation);
		pos = pos.add(destTransformation);
		return pos;
	}


}
