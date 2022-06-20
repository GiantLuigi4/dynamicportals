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
}
