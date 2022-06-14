package tfc.dynamicportals.api;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import org.lwjgl.opengl.GL11;

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
	
	public static boolean isStencilPresent() {
		return isStencilPresent;
	}
	
	private static final RenderType DEPTH_CLEAR = RenderType.create(
			"dynamic_portals_depth_clear",
			DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
			VertexFormat.Mode.TRIANGLE_STRIP,
			256,
			RenderType.CompositeState.builder()
					.setShaderState(RenderType.RENDERTYPE_LEASH_SHADER)
					.setTextureState(RenderType.NO_TEXTURE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.LIGHTMAP)
					.setDepthTestState(RenderType.NO_DEPTH_TEST)
					.createCompositeState(false)
	);
	
	// TODO: this should be cleaned up at some point
	public static void renderPortal(PoseStack a, RenderType type, RenderBuffers buffers, Portal portal, GlStateTracker.State state) {
		PoseStack stack = new PoseStack();
		stack.last().pose().load(a.last().pose());
		stack.last().normal().load(a.last().normal());
		MultiBufferSource.BufferSource source = buffers.bufferSource();
		double rotationX = portal.rotation.x;
		
		Matrix4f srcMat = stack.last().pose();
		Matrix3f norMat = stack.last().normal();
		
		Runnable finishFunc = () -> forceDraw(source);
		
		VertexConsumer consumer;
		stack.translate(portal.position.x, portal.position.y, portal.position.z);
		portal.drawFrame(source, stack);
		finishFunc.run();
		
		rotationX = portal.target.rotation.x;
		stack.mulPose(new Quaternion(0, (float) rotationX, 0, false));
//		if (!portal.isPair) stack.mulPose(new Quaternion(0, 180, 0, true));
		
		Matrix4f portalPose = stack.last().pose().copy();
		
		consumer = source.getBuffer(RenderType.leash());
		stencilTarget.setClearColor(0, 0, 0, 0);
		stencilTarget.clear(Minecraft.ON_OSX);
		stencilTarget.bindWrite(true);
		portal.drawStencil(consumer, portalPose);
		// force draw
		consumer = source.getBuffer(RenderType.lines());
		stencilTarget.unbindWrite();
		
		portalTarget.clear(Minecraft.ON_OSX);
		portalTarget.bindWrite(true);
		consumer = source.getBuffer(RenderType.leash());
		portal.setupStencil(consumer, portalPose, finishFunc);
		
		consumer = source.getBuffer(type);
		
		rotationX = portal.rotation.x;
		stack.mulPose(new Quaternion(0, (float) rotationX, 0, false));
		isStencilPresent = true;
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
		ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
//		stack.translate(0, -1, 0);
		PoseStack temp = new PoseStack();
		temp.last().pose().load(srcMat);
		temp.last().normal().load(norMat);
		temp.mulPose(new Quaternion(0, (float) portal.rotation.x, 0, false));
		temp.mulPose(new Quaternion(0, (float) rotationX, 0, false));
		temp.translate(-portal.target.position.x, -portal.target.position.y, -portal.target.position.z);
		
		PoseStack stk = new PoseStack();
		// copy the matrix
		stk.last().pose().load(temp.last().pose());
		stk.last().normal().load(temp.last().normal());
		Minecraft.getInstance().levelRenderer.renderLevel(
				stk,
				0,
				0,
				false,
				new Camera(),
				Minecraft.getInstance().gameRenderer,
				Minecraft.getInstance().gameRenderer.lightTexture(),
				RenderSystem.getProjectionMatrix()
		);
		// force render
		consumer = source.getBuffer(RenderType.LINES);
		consumer = source.getBuffer(RenderType.leash());
		portal.clearStencil(consumer, portalPose, finishFunc);
		isStencilPresent = false;
		
		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
		consumer = source.getBuffer(RenderType.leash());
		portal.setupStencil(consumer, portalPose, finishFunc);
		consumer = source.getBuffer(RenderType.leash());
		RenderSystem.depthMask(false);
		portalTarget.blitToScreen(portalTarget.width, portalTarget.height, false);
		RenderSystem.depthMask(true);
		portal.clearStencil(consumer, portalPose, finishFunc);
		
		state.restore();
	}
	
	private static void forceDraw(MultiBufferSource source) {
		source.getBuffer(RenderType.LINES);
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
		if (recursion != 0) return;
		
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
		Portal portal = new Portal(new Vector3d(5, 5, 5), new Vector2d(width, 2), null, null, null, null, null, false);
		Portal other = new Portal(new Vector3d(-5, 5, -5), new Vector2d(width, 2), null, null, portal, null, null, true);
		portal.rotation = new Vector2d(Math.toRadians(45 + 180), 0);
		other.rotation = new Vector2d(Math.toRadians(45), 0);
		portal.target = other;
		
		Portal[] portals = new Portal[]{portal, other};
		
		for (Portal portal1 : portals) {
			// TODO: frustum check
			Vector3f normal = computeNormal(portal1);
			if (normal.dot(new Vector3f((float) (camX - portal1.position.x), (float) (camY - portal1.position.y), (float) (camZ - portal1.position.z))) > 0) {
				stack.pushPose();
				renderPortal(stack, type, buffers, portal1, state);
				stack.popPose();
			}
		}
		
		stack.popPose();
		recursion = recursion - 1;
	}
	
	public static Vector3f computeNormal(Portal portal) {
		Vector3f portalPos = new Vector3f((float) portal.position.x, (float) portal.position.y, (float) portal.position.z);
//		Vector3f a = portalPos.copy();
//		a.add((float) -portal.size.x / 2, (float) portal.size.y, 0);
		Vector3f b = portalPos.copy();
		b.add((float) portal.size.x / 2, (float) portal.size.y, 0);
		Vector3f c = portalPos.copy();
		c.add((float) -portal.size.x / 2, 0, 0);
		Vector3f d = portalPos.copy();
		d.add((float) portal.size.x / 2, 0, 0);
		
		Matrix3f matrix3f = new Matrix3f();
		matrix3f.setIdentity();
		matrix3f.mul(new Quaternion(0, (float) portal.rotation.x, 0, false));
//		a.transform(matrix3f);
		b.transform(matrix3f);
		c.transform(matrix3f);
		d.transform(matrix3f);
		
		Vector3f first = b.copy();
		first.sub(d);
		Vector3f second = c.copy();
		second.sub(d);
		
		first.cross(second);
		return first;
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
}
