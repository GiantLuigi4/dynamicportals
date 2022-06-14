package tfc.dynamicportals.api;

import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import net.minecraft.client.renderer.LevelRenderer;
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
	
	// https://gitlab.com/Spectre0987/TardisMod-1-14/-/blob/1.16/src/main/java/net/tardis/mod/client/renderers/boti/BOTIRenderer.java
	public void clearStencil(VertexConsumer consumer, Matrix4f matrix, Runnable finish) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		
		GL11.glColorMask(false, false, false, false);
		GL11.glDepthMask(false);
		drawStencil(consumer, matrix);
		finish.run();

		//Set things back
		GL11.glColorMask(true, true, true, true);
	}
	
	// https://gitlab.com/Spectre0987/TardisMod-1-14/-/blob/1.16/src/main/java/net/tardis/mod/client/renderers/boti/BOTIRenderer.java
	public void setupStencil(VertexConsumer consumer, Matrix4f matrix, Runnable finish) {
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		
		// Always write to stencil buffer
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		
		RenderSystem.enableDepthTest();
		RenderSystem.disableTexture();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		// TODO: color this as the fog color of the portal's pair
		GL11.glColorMask(false, false, false, false);
		drawStencil(consumer, matrix);
		finish.run();
		RenderSystem.enableTexture();
		GL11.glColorMask(true, true, true, true);
		
		// Only pass stencil test if equal to 1(So only if rendered before)
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}
	
	public void drawStencil(VertexConsumer consumer, Matrix4f portalPose) {
		consumer.vertex(portalPose, (float) -this.size.x / 2, (float) this.size.y, 0).color(255, 255, 255, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(portalPose, (float) this.size.x / 2, (float) this.size.y, 0).color(255, 255, 255, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(portalPose, (float) this.size.x / 2, 0, 0).color(255, 255, 255, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
		
		consumer.vertex(portalPose, (float) -this.size.x / 2, 0, 0).color(255, 255, 255, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(portalPose, (float) -this.size.x / 2, (float) this.size.y, 0).color(255, 255, 255, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
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
		this.isPair = isPair;;
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
