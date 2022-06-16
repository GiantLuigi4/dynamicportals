package tfc.dynamicportals.api;

import com.jozufozu.flywheel.repack.joml.Vector2d;
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
import tfc.dynamicportals.DynamicPortals;

import java.util.UUID;

public class BasicPortal extends AbstractPortal {
	public Vector2d size;
	public final boolean isPair;
	public float r = 1, g = r, b = g, a = b;
	
	public static AbstractPortal load(CompoundTag tag) {
		// write position
		Vector3d vec = new Vector3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
		// write size
		Vector2d size = new Vector2d(tag.getDouble("width"), tag.getDouble("height"));
		// write rotation
		Vector2d rotation = new Vector2d(tag.getDouble("yaw"), tag.getDouble("pitch"));
		// write name and uuid
		String name = tag.getString("name");
		UUID uuid = tag.getUUID("uuid");
		// write target and target uuid
		String tname = tag.getString("target");
		UUID tuuid = tag.getUUID("targetUUID");
		// write color
		float r = tag.getFloat("r");
		float g = tag.getFloat("g");
		float b = tag.getFloat("b");
		float a = tag.getFloat("a");
		BasicPortal portal = new BasicPortal(
				uuid, vec, size,
				tuuid, tag.getBoolean("isPair")
		);
		return null;
	}
	
	@Override
	public boolean shouldBeSaved() {
		return true;
	}
	
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
	
	public boolean usesBasicStencil() {
		return true;
	}
	
	public BasicPortal(UUID uuid, Vector3d position, Vector2d size, UUID target, boolean isPair) {
		super(DynamicPortals.BASIC_PORTAL, uuid, target);
		this.position = position;
		this.size = size;
		rotation = new Vector2d();
		this.isPair = isPair;
	}
	
	public CompoundTag toNbt() {
		CompoundTag tag = super.toNbt();
		// write position
		tag.putDouble("x", position.x);
		tag.putDouble("y", position.y);
		tag.putDouble("z", position.z);
		// write size
		tag.putDouble("width", size.x);
		tag.putDouble("height", size.y);
		// write rotation
		tag.putDouble("yaw", rotation.x);
		tag.putDouble("pitch", rotation.y);
		// write name and uuid
		tag.putUUID("uuid", uuid);
		// write target and target uuid
		tag.putUUID("targetUUID", target);
		// write color
		tag.putFloat("r", r);
		tag.putFloat("g", g);
		tag.putFloat("b", b);
		tag.putFloat("a", a);
		// write if it's a pair
		tag.putBoolean("isPair", isPair);
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
