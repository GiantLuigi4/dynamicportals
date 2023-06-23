package tfc.dynamicportals.util.render;

import com.mojang.math.Quaternion;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TransformationInfo {
	public Vec3 pos;
	public Quaternion quaternion;
	public Vec2 look;
	
	public TransformationInfo(Vec3 pos, Quaternion quaternion, Vec2 look) {
		this.pos = pos;
		this.quaternion = quaternion;
		this.look = look;
	}
}
