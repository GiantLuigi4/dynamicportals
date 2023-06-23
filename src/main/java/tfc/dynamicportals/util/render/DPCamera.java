package tfc.dynamicportals.util.render;

import com.mojang.math.Quaternion;

public interface DPCamera {
	boolean useQuat();
	Quaternion getQuat();
}
