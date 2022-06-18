package tfc.dynamicportals;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.Portal;

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
		portalTarget.setClearColor(0, 0, 0, 0);
		portalTarget.clear(Minecraft.ON_OSX);
		portalTarget.bindWrite(false);
		GLUtils.switchFBO(portalTarget);
		isStencilPresent = true;
		// copy matrix so vanilla's renderer doesn't yell at me
		PoseStack stk = new PoseStack();
		stk.last().pose().load(a.last().pose());
		stk.last().normal().load(a.last().normal());
		// setup transform
		portal.negateTransform(stk);
		portal.target.setupAsTarget(stk);
		portal.setupRenderState();
		// setup state
		RenderSystem.enableCull();
		// draw
		Minecraft.getInstance().levelRenderer.renderLevel(stk, Minecraft.getInstance().getFrameTime(), 0, false, new Camera(), Minecraft.getInstance().gameRenderer, Minecraft.getInstance().gameRenderer.lightTexture(), RenderSystem.getProjectionMatrix());
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

//		portalTarget.blitToScreen(
//				Minecraft.getInstance().getMainRenderTarget().width / 5,
//				Minecraft.getInstance().getMainRenderTarget().height / 5
//		);
//		GLUtils.switchFBO(target);
		
		// attempt to reset gl state
		RenderSystem.enableCull();
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
//		source.getBuffer(RenderType.leash());
//		source.getBuffer(RenderType.LINES);
		source.endLastBatch();
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

//		{
//			Portal portal = new Portal();
//			portal.size = new Vector2d(500, 5000);
////			portal.position = new Vector3d(camX, 10, camZ - portal.size.y / 2);
//			portal.position = new Vector3d(0, 0, 0);
//			portal.rotation = new Vector2d(Math.toRadians(22.5), Math.toRadians(0));
////			portal.computeNormal();
//			portals.add(portal);
//		}
		
		Portal other = new Portal()
				.setSize(width, 2)
				.setPosition(-5, 5, -5)
				.setRotation(Math.toRadians(45), 0);
		other.computeNormal();
		portals.add(other);
		{
			Portal portal = new Portal()
					.setSize(width, 2)
					.setPosition(5, 5, 5)
					.setRotation(Math.toRadians(45 + 180), 0);
			portal.computeNormal();
			portals.add(portal);
			other.target = portal;
			portal.target = other;
		}
		
		Frustum frustum = new Frustum(event.getProjectionMatrix(), stack.last().pose());
		for (Portal portal1 : portals) {
			if (portal1.shouldRender(frustum, camX, camY, camZ)) {
				renderPortal(stack, type, buffers, portal1, state);
			}
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
	
	// TODO: this is a bodge
	public static boolean bindPortalFBO(boolean pSetViewport) {
		if (isStencilPresent) {
			portalTarget.bindWrite(pSetViewport);
			return true;
		}
		return false;
	}
}
