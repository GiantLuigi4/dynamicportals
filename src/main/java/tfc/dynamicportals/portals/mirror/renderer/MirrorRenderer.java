package tfc.dynamicportals.portals.mirror.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.implementation.BasicPortalRenderer;
import tfc.dynamicportals.util.Vec2d;
import tfc.dynamicportals.util.gl.GlStateFunctions;

public class MirrorRenderer extends BasicPortalRenderer {
	public MirrorRenderer(BasicPortal portal) {
		super(portal);
	}
	
	@Override
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
		super.drawFrame(source, stack);
		
		
	}
	
	@Override
	public void drawStencil(VertexConsumer builder, PoseStack stack) {
		if (portal.overlaps(Minecraft.getInstance().player.getBoundingBox())) {
			GlStateFunctions.enableDepthClamp();
		}
		
		float padding = 0.1f;
		float zOff = padding / 1;
		
		float r = 1, b = r, g = b, a = g;
		Matrix4f mat = stack.last().pose();
		
		r = 0.85f;
//		r = 1f;
		g = 0.9f;
//		g = 0.1f;
		b = 0.875f;
//		b = 0f;
		float minU = 0, maxU = 0, minV = 0, maxV = 0;
		Vec2d size = portal.getSize();
		
		float texPaddingX = (float) (padding / size.x);
		float texPaddingY = (float) (padding / size.y);
		
		float cr = r;
		float cg = g;
		float cb = b;
		{ // center
			builder.vertex(mat, (float) -size.x / 2 + padding, padding, 0).color(cr, cg, cb, a).uv(minU + texPaddingX, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) size.x / 2 - padding, padding, 0).color(cr, cg, cb, a).uv(maxU - texPaddingX, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) size.x / 2 - padding, (float) size.y - padding, 0).color(cr, cg, cb, a).uv(maxU - texPaddingX, maxV - texPaddingY).endVertex();
			builder.vertex(mat, (float) -size.x / 2 + padding, (float) size.y - padding, 0).color(cr, cg, cb, a).uv(minU + texPaddingX, maxV - texPaddingY).endVertex();
		}
		cr *= 0.9;
		cg *= 0.9;
		cb *= 0.9;
		{ // left
			builder.vertex(mat, (float) size.x / 2, (float) size.y, zOff).color(cr, cg, cb, a).uv(maxU + texPaddingX, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) size.x / 2 - padding, (float) size.y - padding, 0).color(cr, cg, cb, a).uv(maxU - texPaddingY, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) size.x / 2 - padding, padding, 0).color(cr, cg, cb, a).uv(maxU, minV).endVertex();
			builder.vertex(mat, (float) size.x / 2, 0, zOff).color(cr, cg, cb, a).uv(minU, minV).endVertex();
		}
		cr *= 0.9;
		cg *= 0.9;
		cb *= 0.9;
		{ // bottom
			builder.vertex(mat, (float) -size.x / 2 + padding, padding, 0).color(cr, cg, cb, a).uv(maxU + texPaddingX, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) size.x / 2 - padding, padding, 0).color(cr, cg, cb, a).uv(maxU - texPaddingY, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) size.x / 2, 0, zOff).color(cr, cg, cb, a).uv(maxU, minV).endVertex();
			builder.vertex(mat, (float) -size.x / 2, 0, zOff).color(cr, cg, cb, a).uv(minU, minV).endVertex();
		}
		cr = r * 0.95f;
		cg = g * 0.95f;
		cb = b * 0.95f;
		{ // top
			builder.vertex(mat, (float) -size.x / 2, (float) size.y, zOff).color(cr, cg, cb, a).uv(maxU + texPaddingX, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) size.x / 2, (float) size.y, zOff).color(cr, cg, cb, a).uv(maxU - texPaddingY, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) size.x / 2 - padding, (float) size.y - padding, 0).color(cr, cg, cb, a).uv(maxU, minV).endVertex();
			builder.vertex(mat, (float) -size.x / 2 + padding, (float) size.y - padding, 0).color(cr, cg, cb, a).uv(minU, minV).endVertex();
		}
		cr *= 0.9;
		cg *= 0.9;
		cb *= 0.9;
		{ // right
			builder.vertex(mat, (float) -size.x / 2 + padding, (float) size.y - padding, 0).color(cr, cg, cb, a).uv(maxU + texPaddingX, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) -size.x / 2, (float) size.y, zOff).color(cr, cg, cb, a).uv(maxU - texPaddingY, minV + texPaddingY).endVertex();
			builder.vertex(mat, (float) -size.x / 2, 0, zOff).color(cr, cg, cb, a).uv(maxU, minV).endVertex();
			builder.vertex(mat, (float) -size.x / 2 + padding, padding, 0).color(cr, cg, cb, a).uv(minU, minV).endVertex();
		}
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
	
	private void renderFace(Matrix4f pPose, VertexConsumer pConsumer, float pX0, float pX1, float pY0, float pY1, float pZ0, float pZ1, float pZ2, float pZ3) {
		pConsumer.vertex(pPose, pX0, pY0, pZ0).endVertex();
		pConsumer.vertex(pPose, pX1, pY0, pZ1).endVertex();
		pConsumer.vertex(pPose, pX1, pY1, pZ2).endVertex();
		pConsumer.vertex(pPose, pX0, pY1, pZ3).endVertex();
	}
	
	@Override
	public RenderType getRenderType() {
		return super.getRenderType();
//		return STENCIL_DRAW;
	}
}
