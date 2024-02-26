package tfc.dynamicportals.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.util.render.BlendFunctions;

public class NetherPortalRenderer extends BasicPortalRenderer {
	public NetherPortalRenderer(PortalType<BasicPortal> type) {
		super(type);
	}
	
	@Override
	public void drawOverlay(MultiBufferSource.BufferSource source, PoseStack pPoseStack, Camera pCamera, BasicPortal portal, Tesselator tesselator) {
		setupRender(pPoseStack, portal);
		
		int xInt = (int) portal.getSize().x;
		int yInt = (int) portal.getSize().y;
		
		double xSize = portal.getSize().x / 2;
		double ySize = portal.getSize().y / 2;
		
		RenderSystem.setShaderTexture(0, new ResourceLocation("minecraft:textures/block/nether_portal.png"));
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.getShader().apply();
		
		RenderSystem.enableBlend();
		
		BlendFunctions.multiplyBlend();

		float r = 1, b = r, g = b, a = 0.25f;
		r = 120 / 255f;
		g = 0.5f;
		b = 250 / 255f;
		r = (r + 1) / 2;
		b = (b + 1) / 2;
		
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		builder.vertex(
				pPoseStack.last().pose(),
				(float) (-xSize),
				(float) (-ySize),
				(float) (0)
		).color(r, g, b, a).endVertex();
		builder.vertex(
				pPoseStack.last().pose(),
				(float) (-xSize),
				(float) (ySize),
				(float) (0)
		).color(r, g, b, a).endVertex();
		builder.vertex(
				pPoseStack.last().pose(),
				(float) (xSize),
				(float) (ySize),
				(float) (0)
		).color(r, g, b, a).endVertex();
		builder.vertex(
				pPoseStack.last().pose(),
				(float) (xSize),
				(float) (-ySize),
				(float) (0)
		).color(r, g, b, a).endVertex();
		tesselator.end();
		
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.getShader().apply();

		BlendFunctions.exposeBlend();
		
		long frame = Minecraft.getInstance().level.getGameTime();
		frame /= 1;
		frame %= 31;
		float delta = frame / 32f;
		float mU = delta + (1 / 32f);

		float alpha = 1f;
		
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		
		for (int x = 0; x < xInt; x++) {
			for (int y = 0; y < yInt; y++) {
				builder.vertex(
						pPoseStack.last().pose(),
						(float) (-xSize + x),
						(float) (-ySize + y),
						(float) (0)
				).color(1, 1, 1, alpha).uv(0, delta).endVertex();
				builder.vertex(
						pPoseStack.last().pose(),
						(float) (-xSize + x),
						(float) (-ySize + y + 1),
						(float) (0)
				).color(1, 1, 1, alpha).uv(0, mU).endVertex();
				builder.vertex(
						pPoseStack.last().pose(),
						(float) (-xSize + x + 1),
						(float) (-ySize + y + 1),
						(float) (0)
				).color(1, 1, 1, alpha).uv(1, mU).endVertex();
				builder.vertex(
						pPoseStack.last().pose(),
						(float) (-xSize + x + 1),
						(float) (-ySize + y),
						(float) (0)
				).color(1, 1, 1, alpha).uv(1, delta).endVertex();
			}
		}
		
		tesselator.end();
		
		BlendFunctions.alphaBlend();
		
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		
		for (int x = 0; x < xInt; x++) {
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + x),
					(float) (-ySize + 0),
					(float) (0)
			).color(1, 1, 1, alpha).uv(0, delta).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + x),
					(float) (-ySize + 0 + 1),
					(float) (0)
			).color(1, 1, 1, 0).uv(0, mU).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + x + 1),
					(float) (-ySize + 0 + 1),
					(float) (0)
			).color(1, 1, 1, 0).uv(1, mU).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + x + 1),
					(float) (-ySize + 0),
					(float) (0)
			).color(1, 1, 1, alpha).uv(1, delta).endVertex();
			
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + x),
					(float) (-ySize + yInt - 1),
					(float) (0)
			).color(1, 1, 1, 0).uv(0, delta).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + x),
					(float) (-ySize + yInt),
					(float) (0)
			).color(1, 1, 1, alpha).uv(0, mU).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + x + 1),
					(float) (-ySize + yInt),
					(float) (0)
			).color(1, 1, 1, alpha).uv(1, mU).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + x + 1),
					(float) (-ySize + yInt - 1),
					(float) (0)
			).color(1, 1, 1, 0).uv(1, delta).endVertex();
		}
		
		for (int y = 0; y < yInt; y++) {
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + 0),
					(float) (-ySize + y),
					(float) (0)
			).color(1, 1, 1, alpha).uv(0, delta).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + 0),
					(float) (-ySize + y + 1),
					(float) (0)
			).color(1, 1, 1, alpha).uv(0, mU).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + 0 + 1),
					(float) (-ySize + y + 1),
					(float) (0)
			).color(1, 1, 1, 0).uv(1, mU).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + 0 + 1),
					(float) (-ySize + y),
					(float) (0)
			).color(1, 1, 1, 0).uv(1, delta).endVertex();
			
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + xInt - 1),
					(float) (-ySize + y),
					(float) (0)
			).color(1, 1, 1, 0).uv(0, delta).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + xInt - 1),
					(float) (-ySize + y + 1),
					(float) (0)
			).color(1, 1, 1, 0).uv(0, mU).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + xInt),
					(float) (-ySize + y + 1),
					(float) (0)
			).color(1, 1, 1, alpha).uv(1, mU).endVertex();
			builder.vertex(
					pPoseStack.last().pose(),
					(float) (-xSize + xInt),
					(float) (-ySize + y),
					(float) (0)
			).color(1, 1, 1, alpha).uv(1, delta).endVertex();
		}
		
		tesselator.end();
		
		// TODO: decide if this should be used or not
//		BlendFunctions.exposeBlend();
//		alpha = 0.75f;
//		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
//		builder.vertex(
//				pPoseStack.last().pose(),
//				(float) (-xSize),
//				(float) (-ySize),
//				(float) (0)
//		).color(1, 1, 1, alpha).uv(0, delta).endVertex();
//		builder.vertex(
//				pPoseStack.last().pose(),
//				(float) (-xSize),
//				(float) (ySize),
//				(float) (0)
//		).color(1, 1, 1, alpha).uv(0, mU).endVertex();
//		builder.vertex(
//				pPoseStack.last().pose(),
//				(float) (xSize),
//				(float) (ySize),
//				(float) (0)
//		).color(1, 1, 1, alpha).uv(1, mU).endVertex();
//		builder.vertex(
//				pPoseStack.last().pose(),
//				(float) (xSize),
//				(float) (-ySize),
//				(float) (0)
//		).color(1, 1, 1, alpha).uv(1, delta).endVertex();
//		tesselator.end();
		
		BlendFunctions.alphaBlend();
		RenderSystem.disableBlend();
		
		RenderSystem.setShader(GameRenderer::getPositionShader);
		RenderSystem.getShader().apply();
		
		finishRender(pPoseStack, portal);
	}
}
