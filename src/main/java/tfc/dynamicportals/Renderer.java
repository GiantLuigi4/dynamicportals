package tfc.dynamicportals;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.BasicPortal;

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
	
	public static final RenderStateShard.ShaderStateShard RENDERTYPE_LEASH_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader);
	
	private static final RenderType STENCIL_DRAW = RenderType.create(
			"dynamic_portals_stencil",
			DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.QUADS,
			256,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LEASH_SHADER)
					.setTextureState(RenderType.NO_TEXTURE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false)
	);
	
	// TODO: this should be cleaned up at some point
	public static void renderPortal(PoseStack a, RenderType type, RenderBuffers buffers, AbstractPortal portal, GlStateTracker.State state) {
		if (recursion == 2) {
			// TODO: do stuff with this
			return;
		}
		
		// declare variables
		ShaderInstance shaderInstance;
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
		
		// raytracing debug
		Vec3 start = Minecraft.getInstance().cameraEntity.getEyePosition(Minecraft.getInstance().getFrameTime());
		Vec3 end = Minecraft.getInstance().cameraEntity.getLookAngle();
		end = end.scale(8);
		end = start.add(end);
		double dist = portal.trace(start, end);
		Vec3 interp = new Vec3(
				Mth.lerp(dist, start.x, end.x),
				Mth.lerp(dist, start.y, end.y),
				Mth.lerp(dist, start.z, end.z)
		);
		if (dist != 1){
			VertexConsumer consumer = source.getBuffer(RenderType.LINES);
			LevelRenderer.renderLineBox(
					a, consumer,
					interp.x - 0.1, interp.y - 0.1, interp.z - 0.1,
					interp.x + 0.1, interp.y + 0.1, interp.z + 0.1,
					1, 1, 1, 1
			);
			forceDraw(source);
		}
		
		// copy stack (easier to work with, as I don't need to reset the stack's state)
		PoseStack stack = new PoseStack();
		stack.last().pose().load(a.last().pose());
		stack.last().normal().load(a.last().normal());
		
		// setup matrix
		portal.setupMatrix(stack);
		
		// setup stencil
		RenderTarget target = GLUtils.boundTarget();
		stencilTarget.setClearColor(0, 0, 0, 0);
		stencilTarget.clear(Minecraft.ON_OSX);
		GLUtils.switchFBO(stencilTarget);
		portal.drawStencil(source.getBuffer(STENCIL_DRAW), stack);
		forceDraw(source);
		GLUtils.boundTarget().unbindWrite();
		
		// setup to draw to the portal FBO
		portalTarget.clear(Minecraft.ON_OSX);
		portalTarget.bindWrite(false);
		GLUtils.switchFBO(portalTarget);
		isStencilPresent = true;
		// copy matrix so vanilla's renderer doesn't yell at me
		PoseStack stk = new PoseStack();
		stk.last().pose().load(a.last().pose());
		stk.last().normal().load(a.last().normal());
		// setup transform
		portal.setupMatrix(stk);
		portal.target.setupAsTarget(stk);
		portal.setupRenderState();
		// setup state
		RenderSystem.enableCull();
		double camX = Renderer.camX, camY = Renderer.camY, camZ = Renderer.camZ;
		Camera camera = portal.setupCamera(Minecraft.getInstance().gameRenderer.getMainCamera().getEntity(), camX, camY, camZ, Minecraft.getInstance().gameRenderer.getMainCamera());
		stk.translate(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
		
		// draw
		Minecraft.getInstance().levelRenderer.renderLevel(stk, Minecraft.getInstance().getFrameTime(), 0, true, camera, Minecraft.getInstance().gameRenderer, Minecraft.getInstance().gameRenderer.lightTexture(), RenderSystem.getProjectionMatrix());
		// restore camera pos
		Renderer.camX = camX;
		Renderer.camY = camY;
		Renderer.camZ = camZ;
		
		portal.teardownRenderState();
		isStencilPresent = false;
		GLUtils.switchFBO(target);
		
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
		
		// draw portal frame (if there is one)
		portal.drawFrame(source, stack);
		forceDraw(source);
		
		// attempt to reset gl state
		RenderSystem.enableCull();
		Lighting.setupFor3DItems();
		state.restore();
		// TODO: fix the lighting
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
		source.endLastBatch();
	}
	
	private static int recursion = 0;
	
	private static double camX, camY, camZ;
	
	public static void onBeginFrame(BeginFrameEvent event) {
		// store the camera position
		camX = event.getCamera().getPosition().x;
		camY = event.getCamera().getPosition().y;
		camZ = event.getCamera().getPosition().z;
		// setup clear colors
		portalTarget.setClearColor(0, 0, 0, 0);
		portalTarget.setClearColor(0, 0, 0, 0);
	}
	
	public static void onRenderEvent(RenderLevelLastEvent event) {
		if (recursion > 1) return;
		
		/* enable stencils */
		// truthfully this is unused, this is just for compatibility
		if (!Minecraft.getInstance().getMainRenderTarget().isStencilEnabled())
			Minecraft.getInstance().getMainRenderTarget().enableStencil();
		if (!portalTarget.isStencilEnabled())
			portalTarget.enableStencil();
		
		recursion = recursion + 1;
		
		GlStateTracker.State state = GlStateTracker.getRestoreState();
		PoseStack stack = event.getPoseStack();
		Frustum frustum = new Frustum(stack.last().pose(), event.getProjectionMatrix());
		stack.pushPose();
		stack.translate(-camX, -camY, -camZ);
		RenderBuffers buffers = Minecraft.getInstance().renderBuffers();
		RenderType type = RenderType.solid();
		
		BasicPortal[] portals = Temp.getPortals(Minecraft.getInstance().level);
		
		frustum.prepare(camX, camY, camZ);
		for (BasicPortal portal1 : portals) {
			if (portal1.shouldRender(frustum, camX, camY, camZ)) {
				renderPortal(stack, type, buffers, portal1, state);
			}
		}
		
		stack.popPose();
		recursion = recursion - 1;
	}
	
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
	
	// TODO: this is a bodge
	public static boolean bindPortalFBO(boolean pSetViewport) {
		if (isStencilPresent) {
			portalTarget.bindWrite(pSetViewport);
			return true;
		}
		return false;
	}
}
