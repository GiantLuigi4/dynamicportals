package tfc.dynamicportals.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.DynamicPortals;

import java.util.UUID;

/**
 * the base class for all portals
 * {@link BasicPortal} for some examples
 */
public abstract class AbstractPortal {
	public AbstractPortal target = this;
	protected PortalVisibilityGraph graph;
	
	private final UUID uuid;
	
	public AbstractPortal(UUID uuid) {
		this.uuid = uuid;
	}
	
	public void setupVisGraph(LevelRenderer renderer) {
		graph = new PortalVisibilityGraph(renderer);
	}
	
	public PortalVisibilityGraph getGraph() {
		return graph;
	}
	
	/**
	 * sets the target portal, thus creating a linked pair
	 * if a portal is linked to itself, it should be a mirror
	 *
	 * @param target the other portal in the pair
	 */
	public void setTarget(BasicPortal target) {
		this.target = target;
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
	
	/**
	 * more or less the opposite of setupMatrix
	 * I'm not sure why, but currently a 180 degree rotation seems to be needed
	 * {@link DynamicPortals#isRotate180Needed()} will return false if that 180 degree rotation is not needed
	 *
	 * @param stack the pose stack to transform
	 */
	public abstract void setupAsTarget(PoseStack stack);
	
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
	
	/**
	 * @return the offset for the raytrace vector
	 * this should generally be the portal's position
	 */
	public abstract Vec3 raytraceOffset();
	
	/**
	 * @return the quaternion for rotating the look vector for raytracing
	 * this should generally be (TODO)
	 */
	public abstract Quaternion raytraceRotation();
	
	/**
	 * @return whether or not the raytrace rotation needs to be rotated
	 * if the portal is rotated to face the opposite direction of the target portal, the look vector does not need rotation
	 */
	public boolean requireTraceRotation() {
		return true;
	}
	
	/**
	 * raytraces between the start vec and the portal
	 *
	 * @param start the start vector
	 * @param end   the end vector
	 * @return the distance between the start vec and the portal
	 * if it does not hit the portal, it should return 1
	 */
	public abstract double trace(Vec3 start, Vec3 end);
	
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
	 * Sets up the camera for the render info
	 *
	 * @param cameraEntity the entity the player is spectating, or the actual player
	 * @param camX         camera position
	 * @param camY         camera position
	 * @param camZ         camera position
	 * @param gameCamera   the actual camera
	 * @return a dummy camera to use for rendering
	 */
	public abstract Camera setupCamera(Entity cameraEntity, double camX, double camY, double camZ, Camera gameCamera);
	
	public abstract boolean overlaps(AABB box);
	
	// TODO: see if I can get a default implementation for this
	public abstract Vec2 adjustLook(Vec2 vector, boolean reverse);
	
	public boolean isInfront(Entity entity, Vec3 position) {
		return true;
	}
	
	public boolean moveEntity(Entity entity, Vec3 position, Vec3 motion) {
		return false;
	}
}
