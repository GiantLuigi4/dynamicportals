package tfc.dynamicportals.api;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import org.lwjgl.opengl.GL11;
import tfc.dynamicportals.GLUtils;

import java.util.ArrayList;

public class Renderer {
	private static final RenderTarget stencilTarget = new TextureTarget(
			1, 1,
			true, Minecraft.ON_OSX
	);
	private static final RenderTarget portalTarget = new TextureTarget(
			1, 1,
			true, Minecraft.ON_OSX
	);
	
	private static boolean isStencilPresent = false;
	private static boolean screenspaceTex = false;
	
	public static boolean isStencilPresent() {
		return isStencilPresent;
	}
	
	public static boolean useScreenspaceTex() {
		return screenspaceTex;
	}
	
	public static final RenderStateShard.ShaderStateShard RENDERTYPE_LEASH_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorLightmapShader);
	
	private static final RenderType STENCIL_DRAW = RenderType.create(
			"dynamic_portals_stencil",
			DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
			VertexFormat.Mode.QUADS,
			256,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LEASH_SHADER)
					.setTextureState(RenderType.NO_TEXTURE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.LIGHTMAP)
					.createCompositeState(false)
	);
	
	// TODO: this should be cleaned up at some point
	public static void renderPortal(PoseStack a, RenderType type, RenderBuffers buffers, AbstractPortal portal, GlStateTracker.State state) {
		if (recursion == 2) {
			// TODO: do stuff with this
			return;
		}
		
		ShaderInstance shaderInstance;
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
		
		PoseStack stack = new PoseStack();
		stack.last().pose().load(a.last().pose());
		stack.last().normal().load(a.last().normal());
		
//		RenderTarget target = GLUtils.boundTarget();
//		stencilTarget.setClearColor(0, 0, 0, 0);
//		stencilTarget.clear(Minecraft.ON_OSX);
//		GLUtils.switchFBO(stencilTarget);
//		portal.drawStencil(source.getBuffer(STENCIL_DRAW), stack);
//		forceDraw(source);
//		GLUtils.boundTarget().unbindWrite();
//
//		portalTarget.setClearColor(0, 0, 0, 0);
//		portalTarget.clear(Minecraft.ON_OSX);
//		portalTarget.bindWrite(false);
////		GLUtils.switchFBO(portalTarget);
//		isStencilPresent = true;
////		RenderSystem.clearColor(1f, 1, 1, 1);
////		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);
//		portal.drawStencil(source.getBuffer(STENCIL_DRAW), stack);
//		forceDraw(source);
//		GLUtils.switchFBO(target);
//		isStencilPresent = false;
		
		// TODO: WHY ARE THESE FRAME BUFFERS BEING STUPID
		// setup shader
		screenspaceTex = true;
		shaderInstance = GameRenderer.getPositionTexShader();
//		RenderSystem.setShaderTexture(1, stencilTarget.getColorTextureId());
		shaderInstance.setSampler("Sampler0", portalTarget.getColorTextureId());
		shaderInstance.setSampler("DiffuseSampler", portalTarget.getColorTextureId());
		shaderInstance.apply();
		// more setup
		BufferBuilder builder = setupTesselator(shaderInstance, DefaultVertexFormat.POSITION_TEX);
		// draw the portal's stencil
		portal.drawStencil(builder, stack);
		// finish draw
		RenderSystem.disableCull();
		finishTesselator(builder, shaderInstance);
		screenspaceTex = false;
		
//		portalTarget.blitToScreen(
//				Minecraft.getInstance().getMainRenderTarget().width / 5,
//				Minecraft.getInstance().getMainRenderTarget().height / 5
//		);
//		GLUtils.switchFBO(target);
		
//		stencilTarget.blitToScreen(
//				Minecraft.getInstance().getMainRenderTarget().width / 5,
//				Minecraft.getInstance().getMainRenderTarget().height / 5,
//				false
//		);
		
		// restore gl state
		state.restore();
	}
	
	private static BufferBuilder setupTesselator(ShaderInstance shaderInstance, VertexFormat format) {
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, format);
		return builder;
	}
	
	private static void finishTesselator(BufferBuilder builder, ShaderInstance shaderInstance) {
		// enable depth test
		RenderSystem.enableDepthTest();
		// upload
		builder.end();
		BufferUploader._endInternal(builder);
		shaderInstance.clear();
	}
	
	private static void forceDraw(MultiBufferSource.BufferSource source) {
//		source.getBuffer(RenderType.leash());
		source.getBuffer(RenderType.LINES);
//		source.endLastBatch();
	}
	
	private static int recursion = 0;
	
	private static double camX, camY, camZ;
	
	public static void onBeginFrame(BeginFrameEvent event) {
		camX = event.getCamera().getPosition().x;
		camY = event.getCamera().getPosition().y;
		camZ = event.getCamera().getPosition().z;
		portalTarget.setClearColor(0, 0, 0, 0);
	}
	
	public static void onRenderEvent(RenderLevelLastEvent event) {
		if (recursion > 1) return;
		
		/* enable stencils */
		if (!Minecraft.getInstance().getMainRenderTarget().isStencilEnabled())
			Minecraft.getInstance().getMainRenderTarget().enableStencil();
		if (!portalTarget.isStencilEnabled())
			portalTarget.enableStencil();
		
		recursion = recursion + 1;
		
		GlStateTracker.State state = GlStateTracker.getRestoreState();
		PoseStack stack = event.getPoseStack();
		stack.pushPose();
		stack.translate(-camX, -camY, -camZ);
		RenderBuffers buffers = Minecraft.getInstance().renderBuffers();
		RenderType type = RenderType.solid();
		
		double width = Math.sqrt(2 * 4);
		ArrayList<Portal> portals = new ArrayList<>();
		
		portals.add(new Portal());
		
		for (Portal portal1 : portals) {
			// TODO: frustum check
//			Vector3f normal = computeNormal(portal1);
//			if (normal.dot(new Vector3f((float) (camX - portal1.position.x), (float) (camY - portal1.position.y), (float) (camZ - portal1.position.z))) > 0) {
			stack.pushPose();
			renderPortal(stack, type, buffers, portal1, state);
			stack.popPose();
//			}
		}
		
		stack.popPose();
		recursion = recursion - 1;
	}

//	public static Vector3f computeNormal(Portal portal) {
//		Vector3f portalPos = new Vector3f((float) portal.position.x, (float) portal.position.y, (float) portal.position.z);
////		Vector3f a = portalPos.copy();
////		a.add((float) -portal.size.x / 2, (float) portal.size.y, 0);
//		Vector3f b = portalPos.copy();
//		b.add((float) portal.size.x / 2, (float) portal.size.y, 0);
//		Vector3f c = portalPos.copy();
//		c.add((float) -portal.size.x / 2, 0, 0);
//		Vector3f d = portalPos.copy();
//		d.add((float) portal.size.x / 2, 0, 0);
//
//		Matrix3f matrix3f = new Matrix3f();
//		matrix3f.setIdentity();
//		matrix3f.mul(new Quaternion(0, (float) portal.rotation.x, 0, false));
////		a.transform(matrix3f);
//		b.transform(matrix3f);
//		c.transform(matrix3f);
//		d.transform(matrix3f);
//
//		Vector3f first = b.copy();
//		first.sub(d);
//		Vector3f second = c.copy();
//		second.sub(d);
//
//		first.cross(second);
//		return first;
//	}
	
	public static void refreshStencilBuffer(int framebufferWidth, int framebufferHeight) {
		stencilTarget.resize(framebufferWidth, framebufferHeight, Minecraft.ON_OSX);
		portalTarget.resize(framebufferWidth, framebufferHeight, Minecraft.ON_OSX);
	}
	
	public static int getStencilTexture() {
		return stencilTarget.getColorTextureId();
	}
	
	public static int getStencilDepth() {
		return stencilTarget.getDepthTextureId();
	}
	
	public static float fboWidth() {
		return stencilTarget.width;
	}
	
	public static float fboHeight() {
		return stencilTarget.height;
	}
}
