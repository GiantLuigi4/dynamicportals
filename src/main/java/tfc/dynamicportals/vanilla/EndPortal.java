package tfc.dynamicportals.vanilla;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.GLUtils;
import tfc.dynamicportals.Renderer;
import tfc.dynamicportals.api.BasicPortal;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class EndPortal extends BasicPortal {
	public static final RenderStateShard.ShaderStateShard RENDERTYPE_LEASH_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexShader);
	private static final AtomicReference<RenderTarget> targ = new AtomicReference<>();
	private static final RenderStateShard.EmptyTextureStateShard FBOTexture = new RenderStateShard.EmptyTextureStateShard(() -> {
		RenderSystem.enableTexture();
		RenderSystem.setShaderTexture(0, targ.get().getColorTextureId());
	}, () -> {
	});
	private static final RenderType STENCIL_DRAW = RenderType.create(
			"dynamic_portals_stencil",
			DefaultVertexFormat.POSITION_COLOR_TEX,
			VertexFormat.Mode.QUADS,
			256,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LEASH_SHADER)
					.setTextureState(FBOTexture)
					.setCullState(RenderType.NO_CULL)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false)
	);
	SimplexNoise simplexNoise;
	
	public EndPortal(UUID uuid) {
		super(uuid);
		simplexNoise = new SimplexNoise(new XoroshiroRandomSource(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits()));
	}

//	public static final RenderStateShard.ShaderStateShard RENDERTYPE_LEASH_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexShader);
//	private static final RenderType STENCIL_DRAW = RenderType.create(
//			"dynamic_portals_nether_portal_stencil",
//			DefaultVertexFormat.POSITION_COLOR_TEX,
//			VertexFormat.Mode.QUADS,
//			256,
//			RenderType.CompositeState.builder()
//					.setShaderState(RENDERTYPE_LEASH_SHADER)
//					.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true))
//					.setCullState(RenderType.NO_CULL)
//					.setLightmapState(RenderType.NO_LIGHTMAP)
//					.createCompositeState(false)
//	);
	
	@Override
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
		super.drawFrame(source, stack);
		if (targ.get() == null) {
			targ.set(new TextureTarget(1, 1, false, Minecraft.ON_OSX));
		}
		RenderTarget targ = EndPortal.targ.get();
		if (Renderer.fboWidth() != targ.width || Renderer.fboHeight() != targ.height) {
			targ.resize((int) Renderer.fboWidth(), (int) Renderer.fboHeight(), Minecraft.ON_OSX);
		}
		
		double distance = 0.0001 * Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(new Vec3(position.x, position.y, position.z));
		distance = Math.min(distance, 0.1);
		if (!isInfront(Minecraft.getInstance().cameraEntity, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())) {
			stack.scale(-1, 1, -1);
			stack.translate(0, 0, distance);
		}
		
		VertexConsumer consumer = source.getBuffer(RenderType.endPortal());
		RenderTarget bound = GLUtils.boundTarget();
		targ.clear(Minecraft.ON_OSX);
		GLUtils.switchFBO(targ);
		renderFace(stack.last().pose(), consumer, -(float) size.x / 2, (float) size.x / 2, 0, (float) size.y, 0, 0, 0, 0);
		consumer = source.getBuffer(STENCIL_DRAW);
		GLUtils.switchFBO(bound);
		boolean isSS = Renderer.setupScreenspaceTex();
		Matrix4f mat = stack.last().pose();
		consumer.vertex(mat, (float) -size.x / 2, 0, 0).color(1f, 1, 1, 1f / 8).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) size.x / 2, 0, 0).color(1f, 1, 1, 1f / 8).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) size.x / 2, (float) size.y, 0).color(1f, 1, 1, 1f / 8).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) -size.x / 2, (float) size.y, 0).color(1f, 1, 1, 1f / 8).uv(0, 0).endVertex();
		consumer = source.getBuffer(RenderType.endPortal());
		
		consumer = source.getBuffer(STENCIL_DRAW);
		stack.translate(0, 0, distance);
		mat = stack.last().pose();
		consumer.vertex(mat, (float) -size.x / 2, 0, 0).color(1f, 1, 1, 1).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) -size.x / 2 + 0.5f, 0, 0).color(1f, 1, 1, 0).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) -size.x / 2 + 0.5f, (float) size.y, 0).color(1f, 1, 1, 0).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) -size.x / 2, (float) size.y, 0).color(1f, 1, 1, 1).uv(0, 0).endVertex();
		
		consumer.vertex(mat, (float) size.x / 2 - 0.5f, 0, 0).color(1f, 1, 1, 0).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) size.x / 2, 0, 0).color(1f, 1, 1, 1).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) size.x / 2, (float) size.y, 0).color(1f, 1, 1, 1).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) size.x / 2 - 0.5f, (float) size.y, 0).color(1f, 1, 1, 0).uv(0, 0).endVertex();
		consumer = source.getBuffer(RenderType.endPortal());
		
		consumer = source.getBuffer(STENCIL_DRAW);
		stack.translate(0, 0, distance);
		mat = stack.last().pose();
		// top and bottom
		consumer.vertex(mat, (float) -size.x / 2, (float) size.y - 0.5f, 0).color(1f, 1, 1, 0).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) size.x / 2, (float) size.y - 0.5f, 0).color(1f, 1, 1, 0).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) size.x / 2, (float) size.y, 0).color(1f, 1, 1, 1).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) -size.x / 2, (float) size.y, 0).color(1f, 1, 1, 1).uv(0, 0).endVertex();
		
		consumer.vertex(mat, (float) -size.x / 2, 0, 0).color(1f, 1, 1, 1).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) size.x / 2, 0, 0).color(1f, 1, 1, 1).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) size.x / 2, 0.5f, 0).color(1f, 1, 1, 0).uv(0, 0).endVertex();
		consumer.vertex(mat, (float) -size.x / 2, 0.5f, 0).color(1f, 1, 1, 0).uv(0, 0).endVertex();
		
		consumer = source.getBuffer(RenderType.endPortal());
		if (!isSS) Renderer.teardownScreenspaceTex();
	}
	
	private void renderFace(Matrix4f pPose, VertexConsumer pConsumer, float pX0, float pX1, float pY0, float pY1, float pZ0, float pZ1, float pZ2, float pZ3) {
		pConsumer.vertex(pPose, pX0, pY0, pZ0).endVertex();
		pConsumer.vertex(pPose, pX1, pY0, pZ1).endVertex();
		pConsumer.vertex(pPose, pX1, pY1, pZ2).endVertex();
		pConsumer.vertex(pPose, pX0, pY1, pZ3).endVertex();
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
		r = 0.5f;
		g = 0.5f;
		b = 0.5f;
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
