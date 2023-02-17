package tfc.dynamicportals.util.lighting;

import com.mojang.math.Matrix4f;
import tfc.dynamicportals.Renderer;

public class LightingNonsense {
	public static Matrix4f modifyMatrix(Matrix4f matr) {
		if (Renderer.isStencilPresent()) {
			matr = matr.copy();
			matr.setTranslation(0, 0, 0);
			
//			matr.multiply(new Quaternion(0, 180, 0, true));
//			Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
////			matr.multiply(new Quaternion(0, camera.getYRot(), 0, true));
////			matr.multiply(new Quaternion(camera.getXRot(), 0, 0, true));
//			Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
//			rotation.mul(Vector3f.YP.rotationDegrees(camera.getYRot()));
//			rotation.mul(Vector3f.XP.rotationDegrees(-camera.getXRot()));
//			matr.multiply(rotation);
			
			return matr;
		}
		return matr;
	}
}
