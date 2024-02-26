package tfc.dynamicportals.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.client.renderer.AbstractPortalRenderer;
import tfc.dynamicportals.itf.ClientPortalType;
import tfc.dynamicportals.itf.access.ClientLevelAccess;
import tfc.dynamicportals.itf.access.LevelRendererAccessor;
import tfc.dynamicportals.itf.access.MinecraftAccess;
import tfc.dynamicportals.util.DypoShaders;
import tfc.dynamicportals.util.render.RenderUtil;

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
	
	protected void draw(AbstractPortalRenderer renderer, AbstractPortal portal, Minecraft mc, Matrix4f pProjectionMatrix, float pPartialTick, Camera pCamera, GameRenderer pGameRenderer, PoseStack pPoseStack) {
		// actually draw the world
		Frustum frust = mc.levelRenderer.getFrustum();
		
		PoseStack rsStack = RenderSystem.getModelViewStack();
		
		// TODO: investigate NaNs in projection matrix
		Matrix4f portalProj = RenderUtil.getProjectionMatrix(pCamera, pPartialTick, pGameRenderer, mc);
		if (!Double.isNaN(pProjectionMatrix.determinant())) {
			boolean valid = true;
			l:
			for (int i = 0; i < 4; i++) {
				for (int i1 = 0; i1 < 4; i1++) {
					if (Double.isNaN(pProjectionMatrix.get(i, i1))) {
						valid = false;
						break l;
					}
				}
			}
			if (valid)
				portalProj = new Matrix4f(pProjectionMatrix);
		}
		
		RenderSystem.modelViewStack = new PoseStack();
		PoseStack poseCopy = new PoseStack();
		poseCopy.last().pose().set(pPoseStack.last().pose());
		poseCopy.last().normal().set(pPoseStack.last().normal());
		
		
		AbstractPortal target = null;
		for (AbstractPortal abstractPortal : portal.getConnectedNetwork().getPortals())
			if (abstractPortal != portal) target = abstractPortal;
		if (target == null) target = portal;
		// setup render translations
		poseCopy.translate(-pCamera.getPosition().x, -pCamera.getPosition().y, -pCamera.getPosition().z);
		renderer.setupMatrix(portal, poseCopy);
		((ClientPortalType) target.type).getRenderer().setupAsTarget(target, poseCopy);
		poseCopy.translate(pCamera.getPosition().x, pCamera.getPosition().y, pCamera.getPosition().z);
		
		
		AbstractPortal from = RenderUtil.rendering;
		RenderUtil.rendering = target;
		mc.levelRenderer.prepareCullFrustum(
				poseCopy,
				pCamera.getPosition(),
				portalProj
		);
		mc.levelRenderer.renderLevel(
				poseCopy, pPartialTick,
				System.nanoTime(), // idk
				true, // TODO: take this from the arguments of the render level method
				pCamera, pGameRenderer,
				mc.gameRenderer.lightTexture(),
				portalProj
		);
		RenderUtil.rendering = from;
		
		RenderSystem.modelViewStack = rsStack;
		
		((LevelRendererAccessor) mc.levelRenderer).dynamic_portals$setFrustum(frust);
	}
	
	protected void drawSkybox(AbstractPortalRenderer renderer, AbstractPortal portal, Minecraft mc, Matrix4f pProjectionMatrix, float pPartialTick, Camera pCamera, GameRenderer pGameRenderer, PoseStack pPoseStack) {
		float renderDist = pGameRenderer.getRenderDistance();
		boolean foggy = mc.level.effects().isFoggyAt(Mth.floor(pCamera.getPosition().x), Mth.floor(pCamera.getPosition().z)) || mc.gui.getBossOverlay().shouldCreateWorldFog();
		
		FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_SKY, renderDist, foggy, pPartialTick);
		RenderSystem.setShader(GameRenderer::getPositionShader);
		// draw sky
		RenderSystem.setShader(GameRenderer::getPositionShader);
		mc.levelRenderer.renderSky(
				pPoseStack,
				pProjectionMatrix, pPartialTick,
				pCamera, false,
				() -> FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_SKY, renderDist, foggy, pPartialTick)
		);
		GL11.glColorMask(true, true, true, true);
		
		// draw clouds
		FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(renderDist, 32.0F), foggy, pPartialTick);
		FogRenderer.setupColor(pCamera, pPartialTick, mc.level, mc.options.getEffectiveRenderDistance(), pGameRenderer.getDarkenWorldAmount(pPartialTick));
		FogRenderer.levelFogColor();
		RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
		mc.levelRenderer.renderClouds(
				pPoseStack, pProjectionMatrix, pPartialTick,
				pCamera.getPosition().x, pCamera.getPosition().y, pCamera.getPosition().z
		);
	}
	
	@Override
	public void draw(Tesselator tesselator, Minecraft mc, MultiBufferSource.BufferSource source, PoseStack pPoseStack, Matrix4f pProjectionMatrix, Frustum frustum, Camera pCamera, AbstractPortal portal, GameRenderer pGameRenderer, float pPartialTick) {
		int layer = this.layer;
		
		if (frustum.isVisible(portal.getContainingBox())) {
			// translate
			pPoseStack.pushPose();
			pPoseStack.translate(
					-pCamera.getPosition().x,
					-pCamera.getPosition().y,
					-pCamera.getPosition().z
			);
			
			AbstractPortalRenderer renderer = ((ClientPortalType) portal.type).getRenderer();
			
			Minecraft.getInstance().getMainRenderTarget().enableStencil();
			source.endBatch();
			
			RenderType.waterMask().setupRenderState();
			GL40.glEnable(GL40.GL_DEPTH_CLAMP);
			
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
			GL11.glStencilFunc(GL11.GL_EQUAL, layer, 0xFF); // all fragments should pass the stencil test
			GL11.glStencilMask(0xFF); // enable writing to the stencil buffer
			// draw stencil
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			GameRenderer.getRendertypeWaterMaskShader().apply();
			renderer.drawStencil(source, pPoseStack, pCamera, portal, tesselator);
			GL11.glColorMask(true, true, true, true);
			GameRenderer.getRendertypeWaterMaskShader().clear();
			
			GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0xFF);
			GL11.glStencilMask(0x00);
			
			// draw world
			{
				AbstractPortal target = null;
				for (AbstractPortal abstractPortal : portal.getConnectedNetwork().getPortals())
					if (abstractPortal != portal) target = abstractPortal;
				if (target == null) target = portal;
				
				ClientLevel lvl = mc.level;
				LevelRenderer from = mc.levelRenderer;
				mc.level = (ClientLevel) target.myLevel;
				((MinecraftAccess) mc).dynamic_portals$setLevelRenderer(((ClientLevelAccess) mc.level).dynamic_portals$getLevelRenderer());
				
				// reset translation for world render
				
				float renderDist = pGameRenderer.getRenderDistance();
				boolean foggy = mc.level.effects().isFoggyAt(Mth.floor(pCamera.getPosition().x), Mth.floor(pCamera.getPosition().z)) || mc.gui.getBossOverlay().shouldCreateWorldFog();
				
				// clear depth&draw background
				{
					FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_SKY, renderDist, foggy, pPartialTick);
					RenderSystem.setShader(GameRenderer::getPositionShader);
					
					FogRenderer.setupColor(pCamera, pPartialTick, mc.level, mc.options.getEffectiveRenderDistance(), pGameRenderer.getDarkenWorldAmount(pPartialTick));
					FogRenderer.levelFogColor();
					GL11.glDepthFunc(GL11.GL_ALWAYS);
					RenderSystem.setShader(DypoShaders::getDepthClear);
					DypoShaders.getDepthClear().apply();
					float[] fog = RenderSystem.getShaderFogColor();
					RenderSystem.setShaderColor(
							fog[0], fog[1], fog[2], fog[3]
					);
					
					renderer.drawStencil(source, pPoseStack, pCamera, portal, tesselator);
					DypoShaders.getDepthClear().clear();
					GL11.glEnable(GL11.GL_DEPTH_TEST);
					GL11.glDepthFunc(GL11.GL_LEQUAL);
					
					RenderSystem.setShaderColor(1, 1, 1, 1);
				}
				
				GL40.glDisable(GL40.GL_DEPTH_CLAMP);
				pPoseStack.popPose();
				
				// TODO: draw skybox on final iteration
				//       elsewise, draw world
//				if (RenderUtil.activeLayer != 3) {
				if (RenderUtil.activeLayer == 0) {
					draw(renderer, portal, mc, pProjectionMatrix, pPartialTick, pCamera, pGameRenderer, pPoseStack);
				} else {
					drawSkybox(renderer, portal, mc, pProjectionMatrix, pPartialTick, pCamera, pGameRenderer, pPoseStack);
				}
				
				((MinecraftAccess) mc).dynamic_portals$setLevelRenderer(from);
				mc.level = lvl;
			}
			
			// translate back
			pPoseStack.pushPose();
			pPoseStack.translate(
					-pCamera.getPosition().x,
					-pCamera.getPosition().y,
					-pCamera.getPosition().z
			);
			
			GL11.glEnable(GL40.GL_DEPTH_CLAMP);
			
			GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0xFF);
			RenderSystem.disableDepthTest();
			renderer.drawOverlay(source, pPoseStack, pCamera, portal, tesselator);
			
			// TODO: this doesn't work with multiple portals
			//       fix!
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_DECR);
			GL11.glStencilFunc(GL11.GL_EQUAL, layer + 1, 0xFF); // all fragments should pass the stencil test
			GL11.glStencilMask(0xFF); // enable writing to the stencil buffer
			// draw stencil
			GameRenderer.getRendertypeWaterMaskShader().apply();
			RenderType.waterMask().setupRenderState();
			
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
			
			GL11.glColorMask(false, false, false, true);
			renderer.drawStencil(source, pPoseStack, pCamera, portal, tesselator);
			GL11.glColorMask(true, true, true, true);
			RenderType.waterMask().clearRenderState();
			GameRenderer.getRendertypeWaterMaskShader().clear();
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			
			GL11.glDisable(GL40.GL_DEPTH_CLAMP);
			
			GL11.glStencilFunc(GL11.GL_EQUAL, layer, 0xFF);
			GL11.glStencilMask(0x00);
			
			// remove translation
			pPoseStack.popPose();
			
			// fix lighting
			Lighting.setupLevel(pPoseStack.last().pose());
		}
	}
	
	@Override
	public boolean supportsRecurse() {
		return true;
	}
}
