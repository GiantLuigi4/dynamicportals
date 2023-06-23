package tfc.dynamicportals;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import tfc.dynamicportals.access.client.LevelRendererAccessor;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.implementation.PortalRenderSource;
import tfc.dynamicportals.util.VecMath;
import tfc.dynamicportals.util.async.AsyncDispatcher;
import tfc.dynamicportals.util.async.ReusableThread;
import tfc.dynamicportals.util.gl.GlStateFunctions;

import java.util.ArrayList;
import java.util.List;

// TODO: I'd like to make the render systems more modular, so that the renderers can be swapped out easily
// this would be the compat renderer, which aims to not break anything rather than trying to perform well
public class Renderer {
	private static final RenderTarget stencilTarget = new TextureTarget(
			1, 1,
			true, Minecraft.ON_OSX
	);
	private static final RenderTarget portalTarget = new TextureTarget(
			1, 1,
			true, Minecraft.ON_OSX
	);
	private static final List<ReusableThread> threads = new ArrayList<>();
	private static boolean isStencilPresent = false;
	private static boolean screenspaceTex = false;
	private static int recursion = 0;
	private static double camX, camY, camZ;
	private static Vec3 oldPos = new Vec3(0, 0, 0);
	private static double orx = 0;
	private static double ory = 0;
	private static Vec3 camVec = new Vec3(1, 1, 1);
	private static double rx = 0;
	private static double ry = 0;
	
	public static boolean isStencilPresent() {
		return isStencilPresent;
	}
	
	public static boolean useScreenspaceTex() {
		return screenspaceTex;
	}
	
	// TODO: this should be cleaned up at some point
	public static void renderPortal(PoseStack a, RenderType type, RenderBuffers buffers, AbstractPortal portal, Frustum frustum) {
		if (recursion == 2) {
			// Luigi's TODO: do stuff with this
			a.pushPose();
			portal.renderer.setupMatrix(a);
			portal.renderer.drawFrame(buffers.bufferSource(), a);
			a.popPose();
			return;
		}
		
		// declare variables
		//		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		ShaderInstance shaderInstance;
		MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
		GlStateFunctions.enableDepthClamp();
		
		// copy stack (easier to work with, as I don't need to reset the stack's state)
		PoseStack stack = new PoseStack();
		stack.last().pose().load(a.last().pose());
		stack.last().normal().load(a.last().normal());
		
		// raytracing debug
		{
			Entity entity = Minecraft.getInstance().cameraEntity;
			if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
				Vec3 start = entity.getEyePosition(Minecraft.getInstance().getFrameTime());
				Vec3 reach = entity.getViewVector(1.0F).scale(Minecraft.getInstance().gameMode.getPickRange());
				Vec3 end = start.add(reach);
				
				double dist = portal.trace(start, end);
				
				if (dist != 1) {
					Vec3 iStart = VecMath.lerp(dist, start, end);
					Vec3 iReach = VecMath.lerp(dist, reach, Vec3.ZERO);
					
					Vec3 srcOff = portal.raytraceOffset();
					Vec3 dstOff = portal.target.raytraceOffset();
					Quaternion srcRot = portal.raytraceRotation();
					Quaternion dstRot = portal.target.raytraceRotation();
					iStart = VecMath.transform(iStart, srcRot, dstRot, portal.getScaleRatio(), portal.target.get180DegreesRotationAroundVerticalAxis(), portal == portal.target, srcOff, dstOff);
					iReach = VecMath.transform(iReach, srcRot, dstRot, portal.getScaleRatio(), portal.target.get180DegreesRotationAroundVerticalAxis(), portal == portal.target, Vec3.ZERO, Vec3.ZERO);
					
					double size = 0.01;
					
					VertexConsumer consumer = source.getBuffer(RenderType.LINES);
					renderVector(stack, consumer, iStart, iStart.add(iReach), 0, 0, 1);
					renderPoint(stack, consumer, iStart, size, 1, 0, 1);
					renderPoint(stack, consumer, iStart.add(iReach), size, 1, 0, 0);
					forceDraw(source);
				}
			}
		}
		
		// setup matrix
		portal.renderer.setupMatrix(stack);
		
		// setup stencil
		RenderTarget target = GLUtils.boundTarget();
		stencilTarget.setClearColor(0, 0, 0, 0);
		stencilTarget.clear(Minecraft.ON_OSX);
		GLUtils.switchFBO(stencilTarget);
		portal.renderer.drawStencil(source.getBuffer(portal.renderer.getRenderType()), stack);
		forceDraw(source);
		GLUtils.boundTarget().unbindWrite();
		
		// setup to draw to the portal FBO
		portalTarget.clear(Minecraft.ON_OSX);
		portalTarget.bindWrite(false);
		GLUtils.switchFBO(portalTarget);
		
		// setup an actual stencil
		{
			if (!GLUtils.boundTarget().isStencilEnabled())
				GLUtils.boundTarget().enableStencil();
			
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			
			// write to buffer
			GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
			GL11.glStencilMask(0xFF);
			GL11.glColorMask(false, false, false, false);
			GL11.glDepthMask(false);
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

			RenderSystem.enableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.depthFunc(GL11.GL_LEQUAL);

			portal.renderer.drawStencil(source.getBuffer(portal.renderer.getRenderType()), stack);
			forceDraw(source);

			// disable writing
			GL11.glStencilMask(0x00);
			GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);

			GL11.glColorMask(true, true, true, true);
		}
		
		isStencilPresent = true;
		// copy matrix so vanilla's renderer doesn't yell at me
		PoseStack stk = new PoseStack();
		stk.last().pose().load(a.last().pose());
		stk.last().normal().load(a.last().normal());
		// setup transform
		portal.renderer.fullSetupMatrix(stk);
		stk.mulPose(new Quaternion(0, 180, 0, true));
		portal.target.renderer.setupAsTarget(stk);
		portal.renderer.setupRenderState();
		
		// setup state
		RenderSystem.enableCull();
		double camX = Renderer.camX, camY = Renderer.camY, camZ = Renderer.camZ;
		
		// setup camera
		Camera oldCam = Minecraft.getInstance().getEntityRenderDispatcher().camera;
		
		Camera camera = portal.renderer.setupCamera(Minecraft.getInstance().gameRenderer.getMainCamera().getEntity(), camX, camY, camZ, Minecraft.getInstance().gameRenderer.getMainCamera());
		Minecraft.getInstance().getEntityRenderDispatcher().camera = camera;
		stk.translate(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
		
		// swap main render target
		RenderTarget currentTarget = Minecraft.getInstance().getMainRenderTarget();
		Minecraft.getInstance().mainRenderTarget = portalTarget;
		// swap frustum
		LevelRendererAccessor accessor = (LevelRendererAccessor) Minecraft.getInstance().levelRenderer;
		Frustum vanillaFrustum = accessor.getCullingFrustum();
		// TODO: fix this?
//		accessor.setCullingFrustum(new Frustum(RenderSystem.getProjectionMatrix(), stk.last().pose()));
		accessor.setCullingFrustum(getFrustum(portal, a.last().pose(), RenderSystem.getProjectionMatrix()));
		Minecraft.getInstance().levelRenderer.capturedFrustum = null;
		
		// draw
		ObjectArrayList<LevelRenderer.RenderChunkInfo> chunkInfoList = Minecraft.getInstance().levelRenderer.renderChunksInFrustum;
		
		PortalRenderSource renderSource = portal.renderer.getRenderSource();
		if (renderSource != null) {
			renderSource.setActive(true);
			
			PoseStack sysStk = RenderSystem.getModelViewStack();
			
			// don't render vanilla chunks
			Minecraft.getInstance().levelRenderer.renderChunksInFrustum = new ObjectArrayList<>();
			// TODO: I think the level renderer expects a matrix stack that's only a scale/rotation
			Minecraft.getInstance().levelRenderer.renderLevel(stk, Minecraft.getInstance().getFrameTime(), 0, true, camera, Minecraft.getInstance().gameRenderer, Minecraft.getInstance().gameRenderer.lightTexture(), RenderSystem.getProjectionMatrix());
			MinecraftForge.EVENT_BUS.post(new RenderLevelLastEvent(Minecraft.getInstance().levelRenderer, stack, 0, RenderSystem.getProjectionMatrix(), System.nanoTime()));
			RenderSystem.modelViewStack = sysStk;
			RenderSystem.applyModelViewMatrix();
			
			renderSource.setActive(false);
		}
		
		// disable stencil
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		
		accessor.setCullingFrustum(vanillaFrustum);
		Minecraft.getInstance().mainRenderTarget = currentTarget;
		Minecraft.getInstance().levelRenderer.renderChunksInFrustum = chunkInfoList;
		
		// restore camera pos
		Minecraft.getInstance().getEntityRenderDispatcher().camera = oldCam;
		Renderer.camX = camX;
		Renderer.camY = camY;
		Renderer.camZ = camZ;
		
		portal.renderer.teardownRenderState();
		isStencilPresent = false;
		GLUtils.switchFBO(target);
		
		// setup shader
		screenspaceTex = true;
		shaderInstance = portal.renderer.blitShader();
//		RenderSystem.setShaderTexture(1, stencilTarget.getColorTextureId());
		shaderInstance.setSampler("Sampler0", portalTarget.getColorTextureId());
		shaderInstance.setSampler("DiffuseSampler", portalTarget.getColorTextureId());
		shaderInstance.apply();
		// more setup
		BufferBuilder builder = setupTesselator(shaderInstance, portal.renderer.blitFormat());
		// draw the portal's stencil
		portal.renderer.drawStencil(builder, stack);
		// finish draw
		RenderSystem.disableCull();
		finishTesselator(builder, shaderInstance);
		GlStateFunctions.disableDepthClamp(); // reset state
		screenspaceTex = false;
		
		// draw portal frame (if there is one)
		portal.renderer.drawFrame(source, stack);
		forceDraw(source);
		
		// attempt to reset gl state
		RenderSystem.enableCull();
		Lighting.setupFor3DItems();
		// Luigi's TODO: fix the lighting
	}
	
	private static Frustum getFrustum(AbstractPortal portal, Matrix4f mat, Matrix4f matr) {
		PoseStack stk = new PoseStack();
		stk.last().pose().load(mat);
		portal.renderer.setupMatrix(stk);
		stk.mulPose(new Quaternion(0, 180, 0, true));
		portal.target.renderer.setupAsTarget(stk);
		// Luigi's TODO: fix smth here, not sure what?
		return new Frustum(stk.last().pose(), matr);
	}
	
	public static void updatePortal(AbstractPortal portal, Matrix4f mat, Matrix4f proj) {
		// frustum culling
		if (
				portal.renderer.isFirstRender() ||
						(int) orx != (int) rx || (int) ory != (int) ry ||
						(int) oldPos.x != (int) camVec.x ||
						(int) oldPos.y != (int) camVec.y ||
						(int) oldPos.z != (int) camVec.z
		) {
			portal.renderer.getRenderSource().doFrustumUpdate(Minecraft.getInstance().gameRenderer.getMainCamera(), getFrustum(portal, mat, proj));
		}
		
		portal.renderer.setFirstRender(false);
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
	
	public static void preDrawLevel(Camera camera) {
		// store the camera position
		camX = camera.getPosition().x;
		camY = camera.getPosition().y;
		camZ = camera.getPosition().z;
		// setup clear colors
		portalTarget.setClearColor(0, 0, 0, 0);
		portalTarget.setClearColor(0, 0, 0, 0);
		if (recursion == 0) {
			camVec = camera.getPosition();
			rx = camera.getXRot();
			ry = camera.getYRot();
		}
	}
	
	public static void onRenderEvent(RenderLevelLastEvent event) {
		if (recursion > 0)
			return;
		
		RenderTarget bound = GLUtils.boundTarget();
		
		/* enable stencils */
		// truthfully this is unused, this is just for compatibility
		if (!Minecraft.getInstance().getMainRenderTarget().isStencilEnabled())
			Minecraft.getInstance().getMainRenderTarget().enableStencil();
		if (!portalTarget.isStencilEnabled())
			portalTarget.enableStencil();
		
		recursion = recursion + 1;
		
		PoseStack stack = event.getPoseStack();
		Frustum frustum = new Frustum(stack.last().pose(), event.getProjectionMatrix());
		stack.pushPose();
		stack.translate(-camX, -camY, -camZ);
		RenderBuffers buffers = Minecraft.getInstance().renderBuffers();
		RenderType type = RenderType.solid();
		
		AbstractPortal[] portals = Temp.getPortals(Minecraft.getInstance().level);
		
		frustum.prepare(camX, camY, camZ);
		Matrix4f matr = RenderSystem.getProjectionMatrix();
		boolean useAsync = portals.length > 30;
		for (AbstractPortal portal : portals) {
			if (portal.renderer.shouldRender(frustum, camX, camY, camZ)) {
				if (useAsync) {
					AsyncDispatcher.dispatch(() -> {
						updatePortal(portal, event.getPoseStack().last().pose(), matr);
					});
				} else {
					updatePortal(portal, event.getPoseStack().last().pose(), matr);
				}
			}
		}
		AsyncDispatcher.await();
		
		for (AbstractPortal portal1 : portals) {
			if (portal1.renderer.shouldRender(frustum, camX, camY, camZ)) {
				portal1.tickChunkTracking(Minecraft.getInstance().player);
				renderPortal(stack, type, buffers, portal1, frustum);
			}
		}

//		if (!FMLEnvironment.production) {
//			if (Minecraft.getInstance().debugRenderer.renderChunkborder) {
//				if (Minecraft.getInstance().options.renderDebug) {
////					ViewArea area = ((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).getViewArea();
////					ExtendedView extendedView = (ExtendedView) area;
////					VertexConsumer consumer = buffers.bufferSource().getBuffer(RenderType.LINES);
////					for (ChunkRenderDispatcher.RenderChunk value : extendedView.extraView().values()) {
////						LevelRenderer.renderLineBox(
////								stack, consumer,
////								value.getBoundingBox(),
////								1, 0.5f, 0, 1
////						);
////					}
////					forceDraw(buffers.bufferSource());
//				} else {
//					VertexConsumer consumer = buffers.bufferSource().getBuffer(RenderType.LINES);
//					ClientChunkCache cache = Minecraft.getInstance().level.getChunkSource();
//					for (LevelChunk chunk : ((IAmAChunkMap) cache).forcedChunks()) {
//						LevelRenderer.renderLineBox(
//								stack, consumer,
//								chunk.getPos().getMinBlockX(),
//								chunk.getMinBuildHeight(),
//								chunk.getPos().getMinBlockZ(),
//								chunk.getPos().getMaxBlockX() + 1,
//								chunk.getMaxBuildHeight(),
//								chunk.getPos().getMaxBlockZ() + 1,
//								1, 0, 1, 1
//						);
//					}
//					for (LevelChunk chunk : ((IAmAChunkMap) cache).regularChunks()) {
//						if (chunk == null) continue;
//						LevelRenderer.renderLineBox(
//								stack, consumer,
//								chunk.getPos().getMinBlockX(),
//								chunk.getMinBuildHeight(),
//								chunk.getPos().getMinBlockZ(),
//								chunk.getPos().getMaxBlockX() + 1,
//								chunk.getMaxBuildHeight(),
//								chunk.getPos().getMaxBlockZ() + 1,
//								0, 1, 1, 1
//						);
//					}
//					forceDraw(buffers.bufferSource());
//				}
//			}
//		}
		
		stack.popPose();
		recursion = recursion - 1;
		
		if (recursion == 0) {
			orx = Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();
			ory = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
			oldPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		}
		
		GLUtils.switchFBO(bound);
	}
	
	public static void renderPoint(PoseStack stack, VertexConsumer consumer, Vec3 vec, double size, float r, float g, float b) {
		LevelRenderer.renderLineBox(
				stack, consumer,
				vec.x - size, vec.y - size, vec.z - size,
				vec.x + size, vec.y + size, vec.z + size,
				r, g, b, 1
		);
	}
	
	public static void renderVector(PoseStack stack, VertexConsumer consumer, Vec3 start, Vec3 end, float r, float g, float b) {
		Vec3 normal = end.subtract(start);
		consumer.vertex(stack.last().pose(), (float) start.x(), (float) start.y(), (float) start.z()).color(r, g, b, 1).normal(stack.last().normal(), (float) normal.x(), (float) normal.y(), (float) normal.z()).endVertex();
		consumer.vertex(stack.last().pose(), (float) end.x(), (float) end.y(), (float) end.z()).color(r, g, b, 1).normal(stack.last().normal(), (float) normal.x(), (float) normal.y(), (float) normal.z()).endVertex();
	}
	
	public static void renderVector(PoseStack stack, VertexConsumer consumer, Vec3 end, float r, float g, float b) {
		renderVector(stack, consumer, Vec3.ZERO, end, r, g, b);
	}
	
	public static void refreshStencilBuffer(int framebufferWidth, int framebufferHeight) {
		stencilTarget.resize(framebufferWidth, framebufferHeight, Minecraft.ON_OSX); // TODO: try out depth stencil on portal target?
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
	
	// Luigi's TODO: this is a bodge
	public static boolean bindPortalFBO(boolean pSetViewport) {
		if (isStencilPresent) {
			portalTarget.bindWrite(pSetViewport);
			return true;
		}
		return false;
	}
	
	public static void addThread(ReusableThread thread) {
		threads.add(thread);
	}
	
	public static boolean teardownScreenspaceTex() {
		boolean old = screenspaceTex;
		screenspaceTex = false;
		return old;
	}
	
	public static boolean setupScreenspaceTex() {
		boolean old = screenspaceTex;
		screenspaceTex = true;
		return old;
	}
}
