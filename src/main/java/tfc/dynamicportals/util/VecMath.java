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
		Quaternion point = new Quaternion((float) src.x, (float) src.y, (float) src.z, 1);
		Quaternion quat = rotation.copy();
		point.mul(quat);
		quat.conj();
		quat.mul(point);
		return new Vec3(quat.i(), quat.j(), quat.k());
	}

	public static Vec3 start_transform(Vec3 src, Quaternion selfRotation, Quaternion otherRotation, boolean isMirror, boolean motion, Vec3 sourceTransformation, Vec3 destTransformation) {
		if (motion) {
			Vec3 pos = src;
			
			// TODO: this doesn't work properly and I want to scream because of that
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
		pos = start_transform(pos, selfRotation, otherRotation, isMirror, true, sourceTransformation, destTransformation);
		pos = pos.add(destTransformation);
		return pos;
	}

	public static Vec3 reach_transform(Vec3 src, Quaternion selfRotation, Quaternion otherRotation, boolean isMirror, boolean motion, Vec3 sourceTransformation, Vec3 destTransformation) {
		if (motion) {
			Vec3 pos = src;
			// TODO: abstract this to the higher dimensions
			pos = pos.multiply(-1, 1, -1);
			Quaternion otherRotConj = otherRotation.copy();
			otherRotConj.conj();
			Quaternion selfRotConj = selfRotation.copy();
			selfRotConj.conj();

			Vector3f otherRotVec = otherRotConj.toYXZ();
			Vector3f selfRotationVec = selfRotConj.toYXZ();
			otherRotVec.sub(selfRotationVec);
//			System.out.println(otherRotVec);
			pos = pos.yRot(otherRotVec.y());
//			System.out.println(otherRotation.toYXZDegrees() + ", " + otherRotation.toXYZDegrees());
//			System.out.println(selfRotation.toYXZDegrees() + ", " + selfRotation.toXYZDegrees());

//			pos.xRot(otherRotation.toYXZ().y());
//			pos.yRot(otherRotation.toYXZ().x());
//			pos.zRot(otherRotation.toYXZ().z());
//			Quaternion selfConj = selfRotation.copy();
//			selfConj.conj();
//			selfConj.conj();
//			pos = VecMath.rotate(pos, selfConj);
//			System.out.println(selfRotation.toYXZDegrees());
//			Quaternion conj = selfRotation.copy();
//			conj.conj();
//			pos = VecMath.rotate(pos, conj);
//			//pos = VecMath.rotate(pos, Quaternion.fromXYZ(0, 0, (float) Math.PI));
//			pos = pos.multiply(-1, 1, -1);
//			pos = VecMath.rotate(pos, selfRotation);
//			pos = VecMath.rotate(pos, selfRotation);
//			Quaternion otherConj = otherRotation.copy();
//			otherConj.conj();
//			pos = VecMath.rotate(pos, otherConj);

			return pos;
		}
		Vec3 pos = src.subtract(sourceTransformation);
		pos = reach_transform(pos, selfRotation, otherRotation, isMirror, true, sourceTransformation, destTransformation);
		pos = pos.add(destTransformation);
		return pos;
	}


}
