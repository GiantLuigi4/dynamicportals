package tfc.dynamicportals.api.implementation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.GLUtils;
import tfc.dynamicportals.Renderer;
import tfc.dynamicportals.api.PortalRenderer;
import tfc.dynamicportals.util.Quad;
import tfc.dynamicportals.util.TrackyToolsClient;
import tfc.dynamicportals.util.Vec2d;
import tfc.dynamicportals.util.VecMath;

import java.util.ArrayList;

public class BasicPortalRenderer extends PortalRenderer {
	protected BasicPortal portal;
	
	public BasicPortalRenderer(BasicPortal portal) {
		super(portal);
		this.portal = portal;
	}
	
	@Override
	public void drawFrame(MultiBufferSource source, PoseStack stack) {
		VertexConsumer consumer = source.getBuffer(RenderType.LINES);
		
		if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
			if (Minecraft.getInstance().options.renderDebug) {                    /* normal vec debug */
				// absolute position
				stack.pushPose();
				stack.translate(0, (float) portal.size.y / 2, 0);
				stack.mulPose(portal.getActualRotation());
				
				// draw normal vector
				if (portal.renderNormal != null) {
					Renderer.renderVector(stack, consumer, portal.renderNormal, 0, 1, 0);
				} else if (portal.computedNormal != null) {
					Renderer.renderVector(stack, consumer, portal.computedNormal, 1, 0, 1);
				}
				stack.popPose();
				
				//It's actually wrong
//				Vec3 norm = computedNormal;
//				if (norm == null) norm = normal;
//				if (norm != null) {
//					stack.pushPose();
//					stack.translate(0, (float) size.y / 2, 1);
//
//					Quaternion quaternion = getActualRotation();
//					stack.mulPose(quaternion);
//					// luigi: lorenzo wanted this
//
//					//WHAT DO YOU MEAN I DON'T EVEN KNOW WHAT THIS IS
//					// luigi: https://cdn.discordapp.com/attachments/988184753255624774/991124702196154419/unknown.png
//					// left vector
//
//					// I don't need it anymore, you can remove it luigi
//					// luigi: nah, I'm keeping it
//					Vec3 vec = VecMath.rotate(new Vec3(1, 0, 0), quaternion);
//					consumer.vertex(stack.last().pose(), 0, 0, 0).color(1f, 0.5f, 0, 1).normal(1, 0, 0).endVertex();
//					consumer.vertex(stack.last().pose(), (float) vec.x, (float) vec.y, (float) vec.z).color(1f, 0.5f, 0, 1).normal(1, 0, 0).endVertex();
//
//					stack.popPose();
//				}
			}
			
			/* debug frustum culling box */
			stack.pushPose();
			stack.mulPose(portal.getActualRotation());
			
			if (Minecraft.getInstance().options.renderDebug) {
				Quad qd = portal.portalQuad;
				Renderer.renderVector(stack, consumer, qd.pt0, qd.pt1, 1, 0, 0);
				Renderer.renderVector(stack, consumer, qd.pt1, qd.pt2, 1, 1, 0);
				Renderer.renderVector(stack, consumer, qd.pt2, qd.pt3, 1, 0, 1);
				Renderer.renderVector(stack, consumer, qd.pt3, qd.pt0, 0, 1, 1);
				{
					Vec3 eye = Minecraft.getInstance().cameraEntity.getEyePosition();
					eye = eye.subtract(portal.position.x, portal.position.y, portal.position.z);
					Vec3 nearestInQuad = qd.nearestInQuad(eye);
					Vec3 nearestOnEdge = qd.nearestOnEdge(eye);
					Vec3 nearest = qd.nearest(eye);
					Vec3 mid = qd.center();
					double size = 0.005;
					Renderer.renderPoint(stack, consumer, mid, size, 1, 1, 1);
					if (nearestOnEdge != null && nearest != null) {
						if (nearestOnEdge.distanceToSqr(mid) <= nearest.distanceToSqr(mid)) {
							Renderer.renderPoint(stack, consumer, nearestOnEdge, size, 1, 0, nearestInQuad != null ? 1 : 0);
						} else {
							Renderer.renderPoint(stack, consumer, nearest, size, 0, nearestInQuad != null ? 1 : 0, nearestInQuad == null ? 1 : 0);
						}
					} else if (nearest != null) {
						Renderer.renderPoint(stack, consumer, nearest, size, 0, nearestInQuad != null ? 1 : 0, nearestInQuad == null ? 1 : 0);
					}
				}
				
				if (portal != portal.target) {
					//  entity bounding box
					AABB box = Minecraft.getInstance().cameraEntity.getBoundingBox();
					// all the vars
					Quaternion srcQuat = portal.raytraceRotation();
					Quaternion dstQuat = portal.target.raytraceRotation();
					Vec3 srcOff = portal.raytraceOffset();
					Vec3 dstOff = portal.target.raytraceOffset();
					Vec3 pos1 = Minecraft.getInstance().cameraEntity.getPosition(1);
					Vec3 srcPos = pos1;
					pos1 = VecMath.transform(pos1, srcQuat, dstQuat, portal.getScaleRatio(), portal.target.get180DegreesRotationAroundVerticalAxis(), portal != portal.target, srcOff, dstOff);
					
					box = box.move(-srcPos.x, -srcPos.y, -srcPos.z);
					box = box.move(pos1.x, pos1.y, pos1.z);
					
					stack.pushPose();
					stack.translate(-portal.position.x, -portal.position.y, -portal.position.z);
					LevelRenderer.renderLineBox(stack, consumer, box, 0, 0, 1, 1);
					Vec3 center = box.getCenter();
					Vec3 motion = VecMath.transform(Minecraft.getInstance().cameraEntity.getDeltaMovement(), srcQuat, dstQuat, portal.getScaleRatio(), portal.target.get180DegreesRotationAroundVerticalAxis(), false, Vec3.ZERO, Vec3.ZERO);
					stack.translate(center.x, center.y, center.z);
					consumer.vertex(stack.last().pose(), 0, 0, 0).color(1f, 1, 1, 1).normal(1, 0, 0).endVertex();
					stack.scale(10, 10, 10);
					consumer.vertex(stack.last().pose(), (float) motion.x, (float) motion.y, (float) motion.z).color(0, 0, 0, 1).normal(1, 0, 0).endVertex();
					
					stack.popPose();
				}
			}
			
			stack.translate(-portal.position.x, -portal.position.y, -portal.position.z);
			
			// draw
			if (portal.box != null)
				LevelRenderer.renderLineBox(stack, consumer, portal.box.inflate(Minecraft.getInstance().options.renderDebug ? 0.01 : 0), 1, 0, 0, 1);
			stack.popPose();
		}
	}
	
	@Override
	public void drawStencil(VertexConsumer builder, PoseStack stack) {
		float r = 1, b = r, g = b, a = g;
		Matrix4f mat = stack.last().pose();
		// Luigi's TODO: use a custom vertex builder which automatically fills in missing elements
		Vec2d size = portal.size;
		builder.vertex(mat, -((float) size.x / 2), 0, 0).color(r, g, b, a).uv(0, 0).endVertex();
		builder.vertex(mat, ((float) size.x / 2), 0, 0).color(r, g, b, a).uv(0, 0).endVertex();
		builder.vertex(mat, ((float) size.x / 2), (float) size.y, 0).color(r, g, b, a).uv(0, 0).endVertex();
		builder.vertex(mat, -((float) size.x / 2), (float) size.y, 0).color(r, g, b, a).uv(0, 0).endVertex();
	}
	
	@Override
	public void setupMatrix(PoseStack stack) {
		// translate
		Vector3d position = portal.position;
		stack.translate(position.x, position.y, position.z);
		// rotate
		Quaternion quaternion = portal.raytraceRotation();
		if (portal.target == portal) quaternion.mul(new Quaternion(0, -90, 0, true));
		stack.mulPose(quaternion);
	}
	
	@Override
	public void fullSetupMatrix(PoseStack stack) {
		this.setupMatrix(stack);
		
		float xScl = (float) portal.size.x;
		float yScl = (float) portal.size.y;
		stack.scale(xScl, yScl, xScl);
	}
	
	@Override
	public void setupAsTarget(PoseStack stack) {
		float xScl = 1f / (float) portal.size.x;
		float yScl = 1f / (float) portal.size.y;
		
		stack.scale(xScl, yScl, xScl);
		
		boolean isMirror = portal.target == portal;
		Vector3d position = portal.position;
		// rotate
		
		if (isMirror) {
			// mirror
			stack.scale(1, 1, -1);
			// I don't really know why mirrors need this rotation
			stack.mulPose(new Quaternion(0, 180, 0, true));
		}
		stack.mulPose(portal.getActualRotation());
		// translate
		stack.translate(-position.x, -position.y, -position.z);
	}
	
	@Override
	public void setupRenderState() {
		// Luigi's TODO: check if this works well enough
		if (portal == portal.target)
			GLUtils.swapBackface(true);
	}
	
	@Override
	public void teardownRenderState() {
		if (portal == portal.target)
			GLUtils.swapBackface(false);
	}
	
	@Override
	public boolean shouldRender(Frustum frustum, double camX, double camY, double camZ) {
		if (portal.renderNormal == null || portal.renderNormal.dot(new Vec3((camX - portal.position.x), (camY - portal.position.y), (camZ - portal.position.z))) > 0) {
			if (frustum == null) return true;
			return frustum.isVisible(portal.box);
		}
		return false;
	}
	
	@Override
	public void tickForceRendering() {
		// TODO: do level properly, maybe?
		ArrayList<ChunkPos> positions = TrackyToolsClient.getChunksForPortal(Minecraft.getInstance().level, portal);
		ChunkPos center = new ChunkPos(new BlockPos(portal.position.x, portal.position.y, portal.position.z));
		
		ArrayList<ChunkPos> current = new ArrayList<>();
		
		for (int x = -8; x <= 8; x++) {
			for (int z = -8; z <= 8; z++) {
				ChunkPos ps = new ChunkPos(center.x + x, center.z + z);
				boolean pz = positions.remove(ps);
				if (!pz) TrackyToolsClient.markDirty();
				current.add(ps);
			}
		}
		
		if (!positions.isEmpty())
			TrackyToolsClient.markDirty();
		
		positions.clear();
		positions.addAll(current);
	}
}
