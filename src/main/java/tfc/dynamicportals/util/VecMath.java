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
	
	public static Vec3 rotate(Vec3 src, Quaternion rotation) {
		Quaternion point = new Quaternion((float) src.x, (float) src.y, (float) src.z, 1);
		Quaternion quat = rotation.copy();
		point.mul(quat);
		quat.conj();
		quat.mul(point);
		return new Vec3(quat.i(), quat.j(), quat.k());
	}
	
	public static Vec3 transform(Vec3 src, Quaternion selfRotation, Quaternion otherRotation, boolean isMirror, boolean motion, Vec3 sourceTransformation, Vec3 destTransformation) {
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
		pos = transform(pos, selfRotation, otherRotation, isMirror, true, sourceTransformation, destTransformation);
		pos = pos.add(destTransformation);
		return pos;
	}
}
