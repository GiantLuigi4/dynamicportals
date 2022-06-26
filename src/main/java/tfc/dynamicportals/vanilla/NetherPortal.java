package tfc.dynamicportals.vanilla;

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
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.BasicPortal;

import java.util.UUID;

public class NetherPortal extends BasicPortal {
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
	SimplexNoise simplexNoise;
	
	public NetherPortal(UUID uuid) {
		super(uuid);
		simplexNoise = new SimplexNoise(new XoroshiroRandomSource(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits()));
	}
	
	@Override
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
		super.drawFrame(source, stack);
		
		float r = 1, b = r, g = b, a = g;
		Matrix4f mat = stack.last().pose();
		TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("block/nether_portal"));
		// get texture size
		float minU = texture.getU(0);
		float maxU = texture.getU(texture.getWidth());
		float minV = texture.getV(0);
		float maxV = texture.getV(texture.getHeight());
		// compute an offset between each layer so that z fighting isn't noticeable
		double distance = 0.0001 * Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(new Vec3(position.x, position.y, position.z));
		// makes z fighting drastically less noticeable
		distance = Math.min(distance, 0.1);
		if (!isInfront(Minecraft.getInstance().cameraEntity, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())) {
			stack.scale(-1, 1, -1);
		}
		// get ready to draw
		VertexConsumer builder = source.getBuffer(RenderType.translucent());
		// offset to reduce z fighting
		stack.translate(-size.x / 2, 0, distance);
		float shaderTime = Minecraft.getInstance().level.getGameTime();
		shaderTime /= 128;
		// amount to add to all pixels
		// on smaller portals, larger numbers look bad, and on larger portals, the inverse is true
		float add = (float) (size.x + size.y) / (40 * 4);
		if (add > 0.25f) add = 0.25f;
		// compute this outside of the loop to reduce redundant computations
		double xOff = shaderTime / 32 + Math.cos(shaderTime);
		double yOff = shaderTime / 32 + Math.sin(shaderTime);
		float min = (float) Math.cos(shaderTime * 3) / 4;
		// fade out the constant overlay as the player gets further
		float trueMin = (float) (0.2 - (distance * 20));
		// if the pulse is less than the constant overlay, set the pulse to the constant
		if (min < trueMin) min = trueMin;
		// if the value is less than 0.05, it's essentially completely not noticeable
		// therefore, set it to 0 this way the quads don't get rendered if they're basically invisible
		if (min < 0.05) min = 0;
		for (int x = 0; x < size.x; x++) {
			for (int y = 0; y < size.y; y++) {
				float a0 = min;
				float a1 = a0, a2 = a0, a3 = a0;
				// TODO: config
				if (true) {
					// animate transparency
					a0 = (float) simplexNoise.getValue((x + xOff) / 16f, (y + yOff) / 16f, shaderTime) + add;
					a1 = (float) simplexNoise.getValue((x + 1 + xOff) / 16f, (y + yOff) / 16f, shaderTime) + add;
					a2 = (float) simplexNoise.getValue((x + 1 + xOff) / 16f, (y + 1 + yOff) / 16f, shaderTime) + add;
					a3 = (float) simplexNoise.getValue((x + xOff) / 16f, (y + 1 + yOff) / 16f, shaderTime) + add;
					// scale based off size
					a0 *= Math.sqrt(add) * 2;
					a1 *= Math.sqrt(add) * 2;
					a2 *= Math.sqrt(add) * 2;
					a3 *= Math.sqrt(add) * 2;
				}
				// clamping
				if (a0 < min) a0 = min;
				if (a0 > 1) a0 = 1;
				if (a1 < min) a1 = min;
				if (a1 > 1) a1 = 1;
				if (a2 < min) a2 = min;
				if (a2 > 1) a2 = 1;
				if (a3 < min) a3 = min;
				if (a3 > 1) a3 = 1;
				
				// no reason to render it if it's invisible
				if (a0 == 0 && a1 == 0 && a2 == 0 && a3 == 0) continue;
				
				builder
						.vertex(mat, x, y, 0).color(r, g, b, a0)
						.uv(minU, minV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
				builder
						.vertex(mat, x + 1, y, 0).color(r, g, b, a1)
						.uv(maxU, minV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
				builder
						.vertex(mat, x + 1, y + 1, 0).color(r, g, b, a2)
						.uv(maxU, maxV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
				builder
						.vertex(mat, x, y + 1, 0).color(r, g, b, a3)
						.uv(minU, maxV).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 0).endVertex();
			}
		}
		// force draw
		source.getBuffer(RenderType.LINES);
		
		// get ready to draw
		builder = source.getBuffer(RenderType.translucent());
		// offset
		stack.translate(0, 0, distance);
		// horizontal border
		for (int x = 0; x < size.x; x++) {
			// bottom
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
			
			// top
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
		// force draw
		source.getBuffer(RenderType.LINES);
		
		builder = source.getBuffer(RenderType.translucent());
		stack.pushPose();
		// makes z fighting drastically less noticeable
		stack.translate(0, 0, distance);
		mat = stack.last().pose();
		// vertical border
		for (int y = 0; y < size.y; y++) {
			// left
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
			
			// right
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
		builder.vertex(mat, (float) -size.x / 2, 0, 0).color(r, g, b, a).uv(minU, minV).endVertex();
		builder.vertex(mat, (float) size.x / 2, 0, 0).color(r, g, b, a).uv(maxU, minV).endVertex();
		builder.vertex(mat, (float) size.x / 2, (float) size.y, 0).color(r, g, b, a).uv(maxU, maxV).endVertex();
		builder.vertex(mat, (float) -size.x / 2, (float) size.y, 0).color(r, g, b, a).uv(minU, maxV).endVertex();

//		for (int x = (int) -(size.x / 2); x < size.x / 2; x++) {
//			for (int y = 0; y < size.y; y++) {
//				builder.vertex(mat, x, y, 0).color(r, g, b, a).uv(minU, minV).endVertex();
//				builder.vertex(mat, x + 1, y, 0).color(r, g, b, a).uv(maxU, minV).endVertex();
//				builder.vertex(mat, x + 1, y + 1, 0).color(r, g, b, a).uv(maxU, maxV).endVertex();
//				builder.vertex(mat, x, y + 1, 0).color(r, g, b, a).uv(minU, maxV).endVertex();
//			}
//		}
//		new Color(118, 0, 250);
	}
	
	@Override
	public RenderType getRenderType() {
		return super.getRenderType();
//		return STENCIL_DRAW;
	}
}
