package tfc.dynamicportals.api;

import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class Portal {
	public Vector3d position;
	public Vector2d size;
	public Vector2d rotation;
	public final Level sourceLevel, dstLevel;
	public Portal target;
	public final UUID uuid;
	public final ResourceLocation name;
	public final boolean isPair;
	public float r = 1, g = r, b = g, a = b;
	
	public void drawStencil(VertexConsumer consumer, Matrix4f portalPose) {
		// these seem to be good colors for a mirror
		// not sure, but * 0.90f might be better
//		float r = 0.975f * 0.85f;
//		float g = 0.99f * 0.85f;
//		float b = 0.98f * 0.85f;
//		float a = 1;
		consumer.vertex(portalPose, (float) -this.size.x / 2, (float) this.size.y, 0).uv(0, 0).color(r, g, b, a).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(portalPose, (float) this.size.x / 2, (float) this.size.y, 0).uv(0, 0).color(r, g, b, a).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(portalPose, (float) this.size.x / 2, 0, 0).uv(0, 0).color(r, g, b, a).uv2(LightTexture.FULL_BRIGHT).endVertex();
		
		consumer.vertex(portalPose, (float) -this.size.x / 2, 0, 0).uv(0, 0).color(r, g, b, a).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(portalPose, (float) -this.size.x / 2, (float) this.size.y, 0).uv(0, 0).color(r, g, b, a).uv2(LightTexture.FULL_BRIGHT).endVertex();
	}
	
	// whether or not the stencil is just a flat quad which is the width and height of the portal
	// if this is false, it will call drawStencil while drawing the portal to the screen
	public boolean usesBasicStencil() {
		return false;
	}
	
	public Portal(Vector3d position, Vector2d size, Level sourceLevel, Level dstLevel, Portal target, UUID uuid, ResourceLocation name, boolean isPair) {
		this.position = position;
		this.size = size;
		this.sourceLevel = sourceLevel;
		this.dstLevel = dstLevel;
		this.target = target;
		this.uuid = uuid;
		this.name = name;
		rotation = new Vector2d();
		this.isPair = isPair;
		;
	}
	
	public CompoundTag toNbt() {
		CompoundTag tag = new CompoundTag();
		// write position
		tag.putDouble("x", position.x);
		tag.putDouble("y", position.y);
		tag.putDouble("z", position.z);
		// write size
		tag.putDouble("width", size.x);
		tag.putDouble("height", size.y);
		// write rotation
		tag.putDouble("rotation", rotation.x);
		tag.putDouble("pitch", rotation.y);
		// write name and uuid
		tag.putString("name", name.toString());
		tag.putUUID("uuid", uuid);
		// write target and target uuid
		tag.putString("target", target.name.toString());
		tag.putUUID("targetUUID", target.uuid);
		// write color
		tag.putFloat("r", r);
		tag.putFloat("g", g);
		tag.putFloat("b", b);
		tag.putFloat("a", a);
		return tag;
	}
	
	public void drawFrame(MultiBufferSource.BufferSource source, PoseStack stack) {
		VertexConsumer consumer = source.getBuffer(RenderType.lines());
		stack.pushPose();
		double rotationX = rotation.x;
		stack.mulPose(new Quaternion(0, (float) rotationX, 0, false));
		float r = 1;
		float g = 0.5f;
		float b = 0;
		if (isPair) {
			r = g = 0;
			b = 1;
		}
//		LevelRenderer.renderLineBox(stack, consumer, -size.x / 2, 0, 0, size.x / 2, size.y, 0, r, g, b, 1);
		stack.popPose();
	}
}
