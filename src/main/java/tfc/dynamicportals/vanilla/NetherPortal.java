package tfc.dynamicportals.vanilla;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.BasicPortal;

import java.awt.*;
import java.util.UUID;

public class NetherPortal extends BasicPortal {
	public NetherPortal(UUID uuid) {
		super(uuid);
	}
	
	@Override
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
		float r = 1, b = r, g = b, a = g;
		Matrix4f mat = stack.last().pose();
		// TODO: use a custom vertex builder which automatically fills in missing elements
//		TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("block/nether_portal"));
		TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("block/nether_portal"));
		float minU = texture.getU(0);
		float maxU = texture.getU(texture.getWidth());
		float minV = texture.getV(0);
		float maxV = texture.getV(texture.getHeight());
		VertexConsumer builder = source.getBuffer(RenderType.translucent());
		double distance = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(new Vec3(position.x, position.y, position.z));
		stack.translate(0, 0, 0.0001 * distance);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		for (int x = (int) -size.x / 2; x < size.x / 2; x++) {
			builder
					.vertex(mat, x, 0, 0).color(r, g, b, a)
					.uv(minU, minV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, x + 1, 0, 0).color(r, g, b, a)
					.uv(maxU, minV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, x + 1, 0.5f, 0).color(r, g, b, 0)
					.uv(maxU, (minV + maxV) / 2).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, x, 0.5f, 0).color(r, g, b, 0)
					.uv(minU, (minV + maxV) / 2).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			
			builder
					.vertex(mat, x, (float) size.y - 0.5f, 0).color(r, g, b, 0)
					.uv(minU, (minV + maxV) / 2).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, x + 1, (float) size.y - 0.5f, 0).color(r, g, b, 0)
					.uv(maxU, (minV + maxV) / 2).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, x + 1, (float) size.y, 0).color(r, g, b, a)
					.uv(maxU, maxV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, x, (float) size.y, 0).color(r, g, b, a)
					.uv(minU, maxV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
		}
		source.getBuffer(RenderType.LINES);
		
		builder = source.getBuffer(RenderType.translucent());
		stack.pushPose();
		stack.translate(-size.x / 2, 0, 0.0001 * distance);
		mat = stack.last().pose();
		for (int y = 0; y < size.y; y++) {
			builder
					.vertex(mat, 0, y, 0).color(r, g, b, a)
					.uv(minU, minV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, 0.5f, y, 0).color(r, g, b, 0)
					.uv((minU + maxU) / 2, minV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, 0.5f, y + 1, 0).color(r, g, b, 0)
					.uv((minU + maxU) / 2, maxV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, 0, y + 1, 0).color(r, g, b, a)
					.uv(minU, maxV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			
			builder
					.vertex(mat, (float) size.x - 0.5f, y, 0).color(r, g, b, 0)
					.uv((minU + maxU) / 2, minV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, (float) size.x, y, 0).color(r, g, b, a)
					.uv(maxU, minV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, (float) size.x, y + 1, 0).color(r, g, b, a)
					.uv(maxU, maxV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			builder
					.vertex(mat, (float) size.x - 0.5f, y + 1, 0).color(r, g, b, 0)
					.uv((minU + maxU) / 2, maxV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
		}
		stack.popPose();
	}
	
	@Override
	public void drawStencil(VertexConsumer builder, PoseStack stack) {
		float r = 1, b = r, g = b, a = g;
		Matrix4f mat = stack.last().pose();
		// TODO: use a custom vertex builder which automatically fills in missing elements
		TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("block/ice"));
//		float minU = texture.getU(0);
//		float maxU = texture.getU(texture.getWidth());
//		float minV = texture.getV(0);
//		float maxV = texture.getV(texture.getHeight());
		r = 120 / 255f;
		g = 0.5f;
		b = 250 / 255f;
		r = (r + 1) / 2;
		b = (b + 1) / 2;
		float minU = 0, maxU = 0, minV = 0, maxV = 0;
		for (int x = (int) -(size.x / 2); x < size.x / 2; x++) {
			for (int y = 0; y < size.y; y++) {
				builder.vertex(mat, x, y, 0).color(r, g, b, a).uv(minU, minV).endVertex();
				builder.vertex(mat, x + 1, y, 0).color(r, g, b, a).uv(maxU, minV).endVertex();
				builder.vertex(mat, x + 1, y + 1, 0).color(r, g, b, a).uv(maxU, maxV).endVertex();
				builder.vertex(mat, x, y + 1, 0).color(r, g, b, a).uv(minU, maxV).endVertex();
			}
		}
//		new Color(118, 0, 250);
	}
	
	public static final RenderStateShard.ShaderStateShard RENDERTYPE_LEASH_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexShader);
	private static final RenderType STENCIL_DRAW = RenderType.create(
			"dynamic_portals_nether_portal_stencil",
			DefaultVertexFormat.POSITION_COLOR_TEX,
			VertexFormat.Mode.QUADS,
			256,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LEASH_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true))
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false)
	);
	
	@Override
	public RenderType getRenderType() {
		return super.getRenderType();
//		return STENCIL_DRAW;
	}
}
