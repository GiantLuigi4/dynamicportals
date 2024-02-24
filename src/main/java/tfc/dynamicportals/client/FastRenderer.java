package tfc.dynamicportals.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.util.DypoShaders;

public class FastRenderer extends AbstractPortalRenderDispatcher {
	int layer = 0;
	
	@Override
	public void push(int layer) {
		this.layer = layer;
	}
	
	@Override
	public void pop(int layer) {
		this.layer = layer;
	}
	
	@Override
	public void draw(Tesselator tesselator, Minecraft mc, MultiBufferSource.BufferSource source, PoseStack pPoseStack, Matrix4f pProjectionMatrix, Frustum frustum, Camera pCamera, AbstractPortal portal, GameRenderer pGameRenderer, float pPartialTick) {
		if (frustum.isVisible(portal.getContainingBox())) {
			Minecraft.getInstance().getMainRenderTarget().enableStencil();
			source.endBatch();
			
			RenderType.waterMask().setupRenderState();
			GL40.glEnable(GL40.GL_DEPTH_CLAMP);
			
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
			GL11.glStencilFunc(GL11.GL_ALWAYS, layer, 0xFF); // all fragments should pass the stencil test
			GL11.glStencilMask(0xFF); // enable writing to the stencil buffer
			// draw stencil
			GameRenderer.getRendertypeWaterMaskShader().apply();
			drawStencil(pPoseStack, pCamera, portal, tesselator);
			GL11.glColorMask(true, true, true, true);
			GameRenderer.getRendertypeWaterMaskShader().clear();
			
			GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0xFF);
			GL11.glStencilMask(0x00);
			
			GL11.glDepthFunc(GL11.GL_ALWAYS);
			RenderSystem.setShader(DypoShaders::getDepthClear);
			DypoShaders.getDepthClear().apply();
			float[] fog = RenderSystem.getShaderFogColor();
			RenderSystem.setShaderColor(
					fog[0], fog[1], fog[2], fog[3]
			);
			
			drawStencil(pPoseStack, pCamera, portal, tesselator);
			DypoShaders.getDepthClear().clear();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			
			// draw world
			// TODO: actually draw a world
			{
				FogRenderer.levelFogColor();
				float renderDist = pGameRenderer.getRenderDistance();
				boolean foggy = mc.level.effects().isFoggyAt(Mth.floor(pCamera.getPosition().x), Mth.floor(pCamera.getPosition().z)) || mc.gui.getBossOverlay().shouldCreateWorldFog();
				FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_SKY, renderDist, foggy, pPartialTick);
				RenderSystem.setShader(GameRenderer::getPositionShader);
				mc.levelRenderer.renderSky(
						pPoseStack,
						pProjectionMatrix, pPartialTick,
						pCamera, false, () -> FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_SKY, renderDist, foggy, pPartialTick)
				);
				GL11.glColorMask(true, true, true, true);
				FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(renderDist, 32.0F), foggy, pPartialTick);
				RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
				mc.levelRenderer.renderClouds(
						pPoseStack, pProjectionMatrix,
						pPartialTick, pCamera.getPosition().x, pCamera.getPosition().y, pCamera.getPosition().z
				);
			}
			
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_DECR);
			GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0x00); // all fragments should pass the stencil test
			GL11.glStencilMask(0xFF); // enable writing to the stencil buffer
			// draw stencil
			GameRenderer.getRendertypeWaterMaskShader().apply();
			RenderType.waterMask().setupRenderState();
			drawStencil(pPoseStack, pCamera, portal, tesselator);
			RenderType.waterMask().clearRenderState();
			GameRenderer.getRendertypeWaterMaskShader().clear();
			
			GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
			
			GL11.glStencilMask(0xFF);
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
			GL11.glStencilMask(0x00);
			
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			GL11.glDisable(GL40.GL_DEPTH_CLAMP);
		}
	}
	
	void drawStencil(PoseStack pPoseStack, Camera pCamera, AbstractPortal portal, Tesselator tesselator) {
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		builder.vertex(
				pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
				(float) (portal.getPosition().y - pCamera.getPosition().y - 1),
				(float) (portal.getPosition().z - pCamera.getPosition().z - 1)
		).color(255, 0, 0, 255).endVertex();
		builder.vertex(
				pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
				(float) (portal.getPosition().y - pCamera.getPosition().y + 1),
				(float) (portal.getPosition().z - pCamera.getPosition().z - 1)
		).color(255, 0, 0, 255).endVertex();
		builder.vertex(
				pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
				(float) (portal.getPosition().y - pCamera.getPosition().y + 1),
				(float) (portal.getPosition().z - pCamera.getPosition().z + 1)
		).color(255, 0, 0, 255).endVertex();
		builder.vertex(
				pPoseStack.last().pose(), (float) (portal.getPosition().x - pCamera.getPosition().x),
				(float) (portal.getPosition().y - pCamera.getPosition().y - 1),
				(float) (portal.getPosition().z - pCamera.getPosition().z + 1)
		).color(255, 0, 0, 255).endVertex();
		tesselator.end();
	}
}
