package tfc.dynamicportals;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;

public class GLUtils {
	public static void clearColor() {
		float pWidth = Minecraft.getInstance().getMainRenderTarget().width;
		float pHeight = Minecraft.getInstance().getMainRenderTarget().height;
		ShaderInstance shaderinstance = GameRenderer.getPositionColorShader();
		Matrix4f project = RenderSystem.getProjectionMatrix();
		
		Matrix4f matrix4f = Matrix4f.orthographic((float) pWidth, (float) (-pHeight), 1000.0F, 3000.0F);
		RenderSystem.setProjectionMatrix(matrix4f);
		if (shaderinstance.MODEL_VIEW_MATRIX != null) {
			shaderinstance.MODEL_VIEW_MATRIX.set(Matrix4f.createTranslateMatrix(0.0F, 0.0F, -2000.0F));
		}
		
		if (shaderinstance.PROJECTION_MATRIX != null) {
			shaderinstance.PROJECTION_MATRIX.set(matrix4f);
		}
		
		shaderinstance.apply();
		alpha = 1;
		RenderSystem.disableDepthTest();
//		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		GL11.glDepthRange(1, 1);
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferbuilder.vertex(0.0D, pHeight, 0.0D).color(red, green, blue, alpha).endVertex();
		bufferbuilder.vertex(pWidth, pHeight, 0.0D).color(red, green, blue, alpha).endVertex();
		bufferbuilder.vertex(pWidth, 0.0D, 0.0D).color(red, green, blue, alpha).endVertex();
		bufferbuilder.vertex(0.0D, 0.0D, 0.0D).color(red, green, blue, alpha).endVertex();
		bufferbuilder.end();
		BufferUploader._endInternal(bufferbuilder);
		GL11.glDepthRange(0, 1);
		shaderinstance.clear();
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
		
		RenderSystem.setProjectionMatrix(project);
	}
	
	private static float red;
	private static float green;
	private static float blue;
	private static float alpha;
	
	public static void setClearColor(float pRed, float pGreen, float pBlue, float pAlpha) {
		red = pRed;
		green = pGreen;
		blue = pBlue;
		alpha = pAlpha;
	}
	
	public static void clearDepth() {
		// TODO
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
//		float pWidth = Minecraft.getInstance().getMainRenderTarget().width;
//		float pHeight = Minecraft.getInstance().getMainRenderTarget().height;
//		ShaderInstance shaderinstance = GameRenderer.getPositionColorShader();
//		Matrix4f project = RenderSystem.getProjectionMatrix();
//
//		Matrix4f matrix4f = Matrix4f.orthographic((float) pWidth, (float) (-pHeight), 1000.0F, 3000.0F);
//		RenderSystem.setProjectionMatrix(matrix4f);
//		if (shaderinstance.MODEL_VIEW_MATRIX != null) {
//			shaderinstance.MODEL_VIEW_MATRIX.set(Matrix4f.createTranslateMatrix(0.0F, 0.0F, -2000.0F));
//		}
//
//		if (shaderinstance.PROJECTION_MATRIX != null) {
//			shaderinstance.PROJECTION_MATRIX.set(matrix4f);
//		}
//
//		shaderinstance.apply();
//		alpha = 1;
//		RenderSystem.disableDepthTest();
////		RenderSystem.enableDepthTest();
//		RenderSystem.depthMask(true);
//		GlStateManager._colorMask(false, false, false, false);
//		GL11.glDepthRange(1,1);
//		Tesselator tesselator = RenderSystem.renderThreadTesselator();
//		BufferBuilder bufferbuilder = tesselator.getBuilder();
//		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//		bufferbuilder.vertex(0.0D, pHeight, 0.0D).color(red, green, blue, alpha).endVertex();
//		bufferbuilder.vertex(pWidth, pHeight, 0.0D).color(red, green, blue, alpha).endVertex();
//		bufferbuilder.vertex(pWidth, 0.0D, 0.0D).color(red, green, blue, alpha).endVertex();
//		bufferbuilder.vertex(0.0D, 0.0D, 0.0D).color(red, green, blue, alpha).endVertex();
//		bufferbuilder.end();
//		BufferUploader._endInternal(bufferbuilder);
//		GL11.glDepthRange(0, 1);
//		shaderinstance.clear();
//		GlStateManager._depthMask(true);
//		GlStateManager._colorMask(true, true, true, true);
//
//		RenderSystem.setProjectionMatrix(project);
	}
	
	public static void clear(int pMask) {
		if ((pMask & GL11.GL_DEPTH_BUFFER_BIT) == GL11.GL_DEPTH_BUFFER_BIT) clearDepth();
		if ((pMask & GL11.GL_COLOR_BUFFER_BIT) == GL11.GL_COLOR_BUFFER_BIT) clearColor();
		if ((pMask & GL11.GL_STENCIL_BUFFER_BIT) == GL11.GL_STENCIL_BUFFER_BIT) GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
	}
	
	static RenderTarget bound;
	
	public static void setBound(RenderTarget bound) {
		GLUtils.bound = bound;
	}
	
	public static RenderTarget boundTarget() {
		return bound;
	}
	
	public static void switchFBO(RenderTarget target) {
		bound.unbindWrite();
		target.bindWrite(false);
	}
}
