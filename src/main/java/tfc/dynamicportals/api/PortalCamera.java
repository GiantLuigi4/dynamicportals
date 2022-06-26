package tfc.dynamicportals.api;

import net.minecraft.client.Camera;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

public class PortalCamera extends Camera {
	public final Camera actualCamera;
	BlockState cameraBlock = null;
	FogType cameraFog = null;
	
	public PortalCamera(Camera actualCamera) {
		this.actualCamera = actualCamera;
	}
	
	@Override
	public BlockState getBlockAtCamera() {
		if (cameraBlock == null) return super.getBlockAtCamera();
		return cameraBlock;
	}
	
	@Override
	public FogType getFluidInCamera() {
		if (cameraFog == null) return super.getFluidInCamera();
		return cameraFog;
	}
	
	@Override
	public void setPosition(double pX, double pY, double pZ) {
		super.setPosition(pX, pY, pZ);
	}
	
	@Override
	public void setPosition(Vec3 pPos) {
		super.setPosition(pPos);
	}
	
	@Override
	public void setRotation(float pYRot, float pXRot) {
		super.setRotation(pYRot, pXRot);
	}
	
	@Override
	public double getMaxZoom(double pStartingDistance) {
		return actualCamera.getMaxZoom(pStartingDistance);
	}
}
