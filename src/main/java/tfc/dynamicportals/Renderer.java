package tfc.dynamicportals;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.event.BeginFrameEvent;
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
import sun.misc.Unsafe;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.util.VecMath;
import tfc.dynamicportals.util.async.AsyncDispatcher;
import tfc.dynamicportals.util.async.ReusableThread;

import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

public class Renderer {
	private static final RenderTarget stencilTarget = new TextureTarget(
			1, 1,
			true, Minecraft.ON_OSX
	);
	private static final RenderTarget portalTarget = new TextureTarget(
			1, 1,
			true, Minecraft.ON_OSX
	);
	private static final ArrayList<ReusableThread> threads = new ArrayList<>();
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
	public static void renderPortal(PoseStack a, RenderType type, RenderBuffers buffers, AbstractPortal portal, GlStateTracker.State state, Frustum frustum) {
		if (recursion == 2) {
			// TODO: do stuff with this
			return;
		}
		
		// declare variables
		ShaderInstance shaderInstance;
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();

//		// raytracing debug
//		Vec3 start = Minecraft.getInstance().cameraEntity.getEyePosition(Minecraft.getInstance().getFrameTime());
//		Vec3 end = Minecraft.getInstance().cameraEntity.getLookAngle();
//		end = end.scale(8);
//		end = start.add(end);
//		double dist = portal.trace(start, end);
//		Vec3 interp = new Vec3(
//				Mth.lerp(dist, start.x, end.x),
//				Mth.lerp(dist, start.y, end.y),
//				Mth.lerp(dist, start.z, end.z)
//		);
//		if (dist != 1){
//			VertexConsumer consumer = source.getBuffer(RenderType.LINES);
//			LevelRenderer.renderLineBox(
//					a, consumer,
//					interp.x - 0.1, interp.y - 0.1, interp.z - 0.1,
//					interp.x + 0.1, interp.y + 0.1, interp.z + 0.1,
//					1, 1, 1, 1
//			);
//			forceDraw(source);
//		}
		
		// copy stack (easier to work with, as I don't need to reset the stack's state)
		PoseStack stack = new PoseStack();
		stack.last().pose().load(a.last().pose());
		stack.last().normal().load(a.last().normal());
		Vec3 istart = null;
		Vec3 ireach = null;
		Vec3 iend = null;
		
		if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
			Entity entity = Minecraft.getInstance().cameraEntity;
			double reach = Minecraft.getInstance().gameMode.getPickRange();
			Vec3 start = entity.getEyePosition(Minecraft.getInstance().getFrameTime());
			Vec3 look = entity.getViewVector(1.0F);
			Vec3 reachVec = new Vec3(look.x * reach, look.y * reach, look.z * reach);
			Vec3 end = start.add(reachVec);
			
			double dist = portal.trace(start, end);
			
			if (dist != 1) {
				Vec3 interpStart = VecMath.lerp(dist, start, end);
				Vec3 interpReach = VecMath.lerp(1 - dist, Vec3.ZERO, reachVec);
				VertexConsumer consumer = source.getBuffer(RenderType.LINES);
				if (portal.requireTraceRotation()) {
					Quaternion srcQuat = portal.raytraceRotation();
					Quaternion dstQuat = portal.target.oppositeRaytraceRotation();
					Vec3 srcOff = portal.raytraceOffset();
					Vec3 dstOff = portal.target.raytraceOffset();
					
					Vec3 drawnInterpStart = interpStart.subtract(srcOff);
					LevelRenderer.renderLineBox(
							stack, consumer,
							drawnInterpStart.x - 0.01, drawnInterpStart.y - 0.01, drawnInterpStart.z - 0.01,
							drawnInterpStart.x + 0.01, drawnInterpStart.y + 0.01, drawnInterpStart.z + 0.01,
							1, 1, 1, 1
					);
					
					interpStart = VecMath.old_transform(interpStart, srcQuat, dstQuat, portal == portal.target, false, srcOff, dstOff);
					interpReach = VecMath.old_transform(interpReach, srcQuat, dstQuat, portal == portal.target, true, Vec3.ZERO, Vec3.ZERO);
				} else {
					Vec3 offset = portal.target.raytraceOffset().subtract(portal.raytraceOffset());
					interpStart = interpStart.add(offset);
				}
				istart = interpStart;
				ireach = interpReach;
				iend = istart.add(ireach);
				double size = 0.1;
				
				LevelRenderer.renderLineBox(
						stack, consumer,
						istart.x, istart.y, istart.z,
						istart.x + size, istart.y + size, istart.z + size,
						1, 1, 1, 1
				);
				
				forceDraw(source);
			}
		}

//		// TODO: move this off the main thread
//		updatePortal(portal, a.last().pose(), RenderSystem.getProjectionMatrix());
		
		// setup matrix
		portal.setupMatrix(stack);
		
		// setup stencil
		RenderTarget target = GLUtils.boundTarget();
		stencilTarget.setClearColor(0, 0, 0, 0);
		stencilTarget.clear(Minecraft.ON_OSX);
		GLUtils.switchFBO(stencilTarget);
		portal.drawStencil(source.getBuffer(portal.getRenderType()), stack);
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
		stk.mulPose(new Quaternion(0, 180, 0, true));
		portal.target.setupAsTarget(stk);
//		if (DynamicPortals.isRotate180Needed()) stk.mulPose(new Quaternion(0, 180, 0, true));
		portal.setupRenderState();
		// setup state
		RenderSystem.enableCull();
		double camX = Renderer.camX, camY = Renderer.camY, camZ = Renderer.camZ;
		Camera camera = portal.setupCamera(Minecraft.getInstance().gameRenderer.getMainCamera().getEntity(), camX, camY, camZ, Minecraft.getInstance().gameRenderer.getMainCamera());
		Matrix4f matr = stk.last().pose().copy();
		stk.translate(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
		// draw
//		Minecraft.getInstance().levelRenderer.capturedFrustum = portal.getGraph().getFrustum();
		ObjectArrayList<LevelRenderer.RenderChunkInfo> chunkInfoList = Minecraft.getInstance().levelRenderer.renderChunksInFrustum;
		Minecraft.getInstance().levelRenderer.renderChunksInFrustum = portal.getGraph().getChunks();
		Minecraft.getInstance().levelRenderer.renderLevel(stk, Minecraft.getInstance().getFrameTime(), 0, true, camera, Minecraft.getInstance().gameRenderer, Minecraft.getInstance().gameRenderer.lightTexture(), RenderSystem.getProjectionMatrix());
		
		if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
			if (istart != null) {
				VertexConsumer consumer = source.getBuffer(RenderType.LINES);
				consumer.vertex(matr, (float) istart.x, (float) istart.y, (float) istart.z).color(1f, 0, 1, 1).normal(0, 0, 0).endVertex();
				consumer.vertex(matr, (float) iend.x, (float) iend.y, (float) iend.z).color(0f, 0, 1, 1).normal(0, 0, 0).endVertex();

//				LevelRenderer.renderLineBox(
//						stack, consumer,
//						istart.x - 0.01, istart.y - 0.01, istart.z - 0.01,
//						istart.x + 0.01, istart.y + 0.01, istart.z + 0.01,
//						1, 1, 1, 1
//				);
				forceDraw(source);
			}
		}
		
		Minecraft.getInstance().levelRenderer.renderChunksInFrustum = chunkInfoList;
		// restore camera pos
		Renderer.camX = camX;
		Renderer.camY = camY;
		Renderer.camZ = camZ;
		
		portal.teardownRenderState();
		isStencilPresent = false;
		GLUtils.switchFBO(target);
		
		// setup shader
		screenspaceTex = true;
		shaderInstance = portal.blitShader();
//		RenderSystem.setShaderTexture(1, stencilTarget.getColorTextureId());
		shaderInstance.setSampler("Sampler0", portalTarget.getColorTextureId());
		shaderInstance.setSampler("DiffuseSampler", portalTarget.getColorTextureId());
		shaderInstance.apply();
		// more setup
		BufferBuilder builder = setupTesselator(shaderInstance, portal.blitFormat());
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
	
	private static Frustum getFrustum(AbstractPortal portal, Matrix4f mat, Matrix4f matr) {
		PoseStack stk = new PoseStack();
		stk.last().pose().load(mat);
		portal.setupMatrix(stk);
		stk.mulPose(new Quaternion(0, 180, 0, true));
		portal.target.setupAsTarget(stk);
		// TODO: fix smth here, not sure what?
		Frustum frustum1 = new Frustum(stk.last().pose(), matr);
		return frustum1;
	}
	
	public static void updatePortal(AbstractPortal portal, Matrix4f mat, Matrix4f proj) {
		// frustum culling
		if (portal.getGraph() == null) {
			portal.setupVisGraph(Minecraft.getInstance().levelRenderer);
			portal.getGraph().setFrustum(getFrustum(portal, mat, proj));
			portal.getGraph().update();
		} else if (
				(int) orx != (int) rx || (int) ory != (int) ry ||
						(int) oldPos.x != (int) camVec.x ||
						(int) oldPos.y != (int) camVec.y ||
						(int) oldPos.z != (int) camVec.z
		) {
			portal.setupVisGraph(Minecraft.getInstance().levelRenderer);
			portal.getGraph().setFrustum(getFrustum(portal, mat, proj));
			portal.getGraph().update();
		}
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
	
	public static void onBeginFrame(BeginFrameEvent event) {
		// store the camera position
		camX = event.getCamera().getPosition().x;
		camY = event.getCamera().getPosition().y;
		camZ = event.getCamera().getPosition().z;
		// setup clear colors
		portalTarget.setClearColor(0, 0, 0, 0);
		portalTarget.setClearColor(0, 0, 0, 0);
		if (recursion == 0) {
			camVec = event.getCamera().getPosition();
			rx = event.getCamera().getXRot();
			ry = event.getCamera().getYRot();
		}
	}
	
	public static void onRenderEvent(RenderLevelLastEvent event) {
		if (recursion > 1) return;
		
		for (ReusableThread thread : threads) {
			thread.await();
		}
		
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
		
		AbstractPortal[] portals = Temp.getPortals(Minecraft.getInstance().level);
		
		frustum.prepare(camX, camY, camZ);
		Matrix4f matr = RenderSystem.getProjectionMatrix();
		boolean useAsync = portals.length > 30;
		for (AbstractPortal portal : portals) {
			if (useAsync) {
				AsyncDispatcher.dispatch(() -> {
					updatePortal(portal, event.getPoseStack().last().pose(), matr);
				});
			} else {
				updatePortal(portal, event.getPoseStack().last().pose(), matr);
			}
		}
		AsyncDispatcher.await();
		
		for (AbstractPortal portal1 : portals) {
			if (portal1.shouldRender(frustum, camX, camY, camZ)) {
				renderPortal(stack, type, buffers, portal1, state, frustum);
			}
		}
		
		stack.popPose();
		recursion = recursion - 1;
		
		if (recursion == 0) {
			orx = Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();
			ory = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
			oldPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		}
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
