package tfc.dynamicportals.api;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.nbt.CompoundTag;

public abstract class AbstractPortal {
	public abstract void drawStencil(VertexConsumer consumer, Matrix4f portalPose);
	
	public VertexFormat vertexFormat() {
		return DefaultVertexFormat.POSITION_COLOR;
	}
	
	// if you want to setup shader uniforms, this is the place to do it
	public ShaderInstance getShader() {
		return GameRenderer.getPositionColorShader();
	}
	
	// whether or not the stencil is just a flat quad which is the width and height of the portal
	// if this is false, it will call drawStencil while drawing the portal to the screen
	public boolean usesBasicStencil() {
		return true;
	}
	
	public abstract CompoundTag toNbt();
	
	public void drawFrame(MultiBufferSource.BufferSource source, PoseStack stack) {
	}
}