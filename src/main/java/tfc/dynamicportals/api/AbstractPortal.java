package tfc.dynamicportals.api;

import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.UUID;

public abstract class AbstractPortal {
	public Vector3d position;
	public Vector2d rotation;
	public final PortalType type;
	public final UUID uuid;
	public UUID target;
	
	public AbstractPortal(PortalType type, UUID uuid, UUID target) {
		this.type = type;
		this.uuid = uuid;
		this.target = target;
	}
	
	// whatever you draw here is what the shape of the portal will be
	public abstract void drawStencil(VertexConsumer consumer, Matrix4f portalPose);
	
	// whether or not the portal should be saved to the world
	public abstract boolean shouldBeSaved();
	
	// what vertex format should be used for the stencil
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
	
	// the result of this gets saved to the level
	public CompoundTag toNbt() {
		CompoundTag tag = new CompoundTag();
		tag.putString("type", type.getName().toString());
		return null;
	}
	
	public void drawFrame(MultiBufferSource.BufferSource source, PoseStack stack) {
	}
}
