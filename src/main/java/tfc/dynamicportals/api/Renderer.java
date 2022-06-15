package tfc.dynamicportals.api;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderLevelLastEvent;

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
		if (recursion == 2) {
			// TODO: do stuff with this
			return;
		}
		
		PoseStack stack = new PoseStack();
		stack.last().pose().load(a.last().pose());
		stack.last().normal().load(a.last().normal());
		MultiBufferSource.BufferSource source = buffers.bufferSource();
		Runnable finishFunc = () -> forceDraw(source);
		
		stack.translate(portal.position.x, portal.position.y, portal.position.z);
		
		stack.pushPose();
		double rotationX = portal.rotation.x;
		stack.mulPose(new Quaternion(0, (float) rotationX, 0, false));
		
		/* setup stencil */
		Minecraft.getInstance().getMainRenderTarget().unbindWrite();
		stencilTarget.setClearColor(0, 0, 0, 0);
		stencilTarget.clear(Minecraft.ON_OSX);
		stencilTarget.bindWrite(true);
		
		ShaderInstance shaderInstance = GameRenderer.getPositionColorShader();
		BufferBuilder buffer = setupTesselator(shaderInstance, DefaultVertexFormat.POSITION_COLOR);
		shaderInstance.apply();
		// TODO: get this to work with tesselator
		portal.drawStencil(source.getBuffer(RenderType.leash()), stack.last().pose());
		finishTesselator(buffer, shaderInstance);
		finishFunc.run();
		
		stencilTarget.unbindWrite();
		
		portalTarget.clear(Minecraft.ON_OSX);
		portalTarget.bindWrite(true);
		/* Camera manipulation */
		Camera camera = new Camera();
		Entity entity = Minecraft.getInstance().cameraEntity;
		double xo = entity.xo;
		double yo = entity.yo;
		double zo = entity.zo;
		entity.xo -= Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().x;
		entity.xo += portal.target.position.x;
		entity.zo -= Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().z;
		entity.zo += portal.target.position.z;
		// TODO: fix this mess
		double oldY = -yo;
		oldY += Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y;
		oldY += portal.target.position.y;
		entity.yo = oldY;
		double x = entity.position().x;
		double y = entity.position().y;
		double z = entity.position().z;
		entity.setPosRaw(
				portal.target.position.x,
//				portal.target.position.y,
				oldY,
				portal.target.position.z
		);
		camera.setup(
				Minecraft.getInstance().level,
				entity,
				false, false,
				Minecraft.getInstance().getFrameTime()
		);
		entity.setPosRaw(x, y, z);
		entity.xo = xo;
		entity.yo = yo;
		entity.zo = zo;
		double cx = camX;
		double cy = camY;
		double cz = camZ;
		PoseStack stk = new PoseStack();
		/* Matrix Manipulation */
		stk.last().pose().load(stack.last().pose());
		stk.last().normal().load(stack.last().normal());
		stk.mulPose(new Quaternion(0, (float) -rotationX * 2, 0, false));
		stk.mulPose(new Quaternion(0, (float) portal.target.rotation.x, 0, false));
		stk.mulPose(new Quaternion(0, 180, 0, true));
		stk.translate(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
		stk.translate(-portal.target.position.x, -portal.target.position.y, -portal.target.position.z);
		isStencilPresent = true;
		/* Draw */
		Minecraft.getInstance().levelRenderer.renderLevel(stk, Minecraft.getInstance().getFrameTime(), 0, true, camera, Minecraft.getInstance().gameRenderer, Minecraft.getInstance().gameRenderer.lightTexture(), RenderSystem.getProjectionMatrix());
		/* Reset */
		isStencilPresent = false;
		camX = cx;
		camY = cy;
		camZ = cz;
		portalTarget.unbindWrite();
		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
		
		/* Display portal */
		// setup shader
		shaderInstance = GameRenderer.getPositionTexShader();
		RenderSystem.setShaderTexture(1, portalTarget.getColorTextureId());
		screenspaceTex = true;
		shaderInstance.setSampler("Sampler0", portalTarget.getColorTextureId());
		shaderInstance.apply();
		// more setup
		BufferBuilder builder = setupTesselator(shaderInstance, DefaultVertexFormat.POSITION_TEX);
		Matrix4f mat = stack.last().pose().copy();
		Vector4f vec;
		// draw quad
		vec = new Vector4f(-((float) portal.size.x / 2), 0, 0, 1);
		builder.vertex(mat, vec.x(), vec.y(), vec.z()).uv(0, 0).endVertex();
		vec = new Vector4f(((float) portal.size.x / 2), 0, 0, 1);
		builder.vertex(mat, vec.x(), vec.y(), vec.z()).uv(0, 0).endVertex();
		vec = new Vector4f(((float) portal.size.x / 2), (float) portal.size.y, 0, 1);
		builder.vertex(mat, vec.x(), vec.y(), vec.z()).uv(0, 0).endVertex();
		vec = new Vector4f(-((float) portal.size.x / 2), (float) portal.size.y, 0, 1);
		builder.vertex(mat, vec.x(), vec.y(), vec.z()).uv(0, 0).endVertex();
		// finish draw
		finishTesselator(builder, shaderInstance);
		
		screenspaceTex = false;
		
		stack.popPose();
//		stack.mulPose(new Quaternion(0, (float) -portal.rotation.x, 0, false));
		// draw frame
		portal.drawFrame(source, stack);
		finishFunc.run();
		
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
	
	private static void forceDraw(MultiBufferSource source) {
		source.getBuffer(RenderType.leash());
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
		{
			Portal portal = new Portal(new Vector3d(5, 5, 5), new Vector2d(width, 2), null, null, null, null, null, false);
			Portal other = new Portal(new Vector3d(-5, 5, -5), new Vector2d(width, 2), null, null, portal, null, null, true);
			portal.rotation = new Vector2d(Math.toRadians(45 + 180), 0);
			other.rotation = new Vector2d(Math.toRadians(45), 0);
			portal.target = other;
			portals.add(portal);
			portals.add(other);
		}
		{
			Portal portal = new Portal(new Vector3d(0, 5, 5.001), new Vector2d(2, 2), null, null, null, null, null, false);
//			Portal other = new Portal(new Vector3d(0, 5, -5), new Vector2d(2, 2), null, null, portal, null, null, true);
//			other.rotation = new Vector2d(Math.toRadians(0), 0);
			portal.rotation = new Vector2d(Math.toRadians(180), 0);
			portal.target = portal;
			portals.add(portal);
//			portals.add(other);
		}
		
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
