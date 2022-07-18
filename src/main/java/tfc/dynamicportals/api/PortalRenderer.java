package tfc.dynamicportals.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.util.RenderTypes;

public abstract class PortalRenderer {
	protected PortalVisibilityGraph graph;
	protected AbstractPortal portal;
	
	public PortalRenderer(AbstractPortal portal) {
		this.portal = portal;
	}
	
	public void setupVisGraph(LevelRenderer renderer) {
		graph = new PortalVisibilityGraph(renderer);
	}
	
	public PortalVisibilityGraph getGraph() {
		if (graph != null) {
			Vec3 offset = portal.raytraceOffset();
			graph.originX = (int) offset.x;
			graph.originY = (int) offset.y;
			graph.originZ = (int) offset.z;
		}
		return graph;
	}
	
	public void teleportEntity(Entity entity) {
		if (entity == Minecraft.getInstance().cameraEntity) {
			if (graph != null)
				graph.nudgeRenderer();
		}
	}
	
	/**
	 * if the portal has a "frame", this is where you'd draw it
	 * if you want to know what a "frame" is, go look at the game portal
	 *
	 * @param source the source of the vertex consumers
	 * @param stack  the pose stack, already transformed based off {@link #setupMatrix(PoseStack)}
	 */
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
	}
	
	/**
	 * this is where you'd setup the render state for the portal
	 * an example of this is switching which face is the back face for a mirror
	 */
	public void setupRenderState() {
	}
	
	/**
	 * undo state changes done in {@link #setupRenderState()}
	 */
	public void teardownRenderState() {
	}
	
	/**
	 * Draws the stencil shape
	 * basically, whatever you draw from here is what the portal's shape is
	 * this should generally be a 2D plane on the x and y axes
	 *
	 * @param builder the vertex consumer, which is currently either:
	 *                POSITION_COLOR or POSITION_TEX
	 *                when it's POSITION_TEX, the texture coords don't matter, so you can just do 0,0
	 * @param stack   the pose stack, already transformed based off {@link AbstractPortal#setupMatrix(PoseStack)}
	 */
	public abstract void drawStencil(VertexConsumer builder, PoseStack stack);
	
	/**
	 * setup the matrix stack for rendering the shape of the portal
	 * assuming you know matrix math, this should be pretty straight forwards
	 * basically, it should just offset the matrix to the portal and rotate it accordingly
	 * this is also used for position the camera at 0,0,0 facing 0,0,1 (I think)
	 *
	 * @param stack the pose stack to transform
	 */
	public abstract void setupMatrix(PoseStack stack);
	
	public abstract void fullSetupMatrix(PoseStack stack);
	
	/**
	 * more or less the opposite of setupMatrix
	 * I'm not sure why, but currently a 180 degree rotation seems to be needed
	 *
	 * @param stack the pose stack to transform
	 */
	public abstract void setupAsTarget(PoseStack stack);
	
	/**
	 * the render type for the stencil
	 * this is used if you want to, for example, use a texture for the multiplicative overlay
	 *
	 * @return the stencil render type
	 */
	public RenderType getRenderType() {
		return RenderTypes.STENCIL_DRAW;
	}
	
	/**
	 * I am not responsible for any problems caused by overriding this
	 * <p>
	 * the return value of this should match {@link #blitShader()}'s vertex format
	 *
	 * @return the vertex format for the blit pass
	 */
	public VertexFormat blitFormat() {
		return blitShader().getVertexFormat();
	}
	
	
	/**
	 * I am not responsible for any problems caused by overriding this
	 * this allows you to use a custom shader on the portal's image
	 * <p>
	 * if you need dynamic portal's automatically injected shader code to not be injected,
	 * include an "#dynportals skip_inject" anywhere in your shader file
	 *
	 * @return a shader to use for rendering the portal
	 */
	public ShaderInstance blitShader() {
		return GameRenderer.getPositionTexShader();
	}
	
	/**
	 * whether or not the portal should render
	 * this should take into account backface culling, as well as frustum checking
	 *
	 * @param frustum the frustum to check with
	 * @param camX    the camera position
	 * @param camY    see camX
	 * @param camZ    see camX
	 * @return if the portal should render
	 */
	public abstract boolean shouldRender(Frustum frustum, double camX, double camY, double camZ);
}
