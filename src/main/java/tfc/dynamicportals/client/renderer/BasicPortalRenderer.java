package tfc.dynamicportals.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.util.render.BlendFunctions;
import tfc.dynamicportals.util.render.RenderUtil;

import java.lang.Math;

public class BasicPortalRenderer extends AbstractPortalRenderer<BasicPortal> {
	public BasicPortalRenderer(PortalType<BasicPortal> type) {
		super(type);
	}
	
	protected void setupRender(PoseStack pPoseStack, BasicPortal portal) {
		if (portal.isDoubleSided()) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
		
		pPoseStack.pushPose();
		portal.setSize(new Vector2d(2, 2));
		pPoseStack.translate(portal.getPosition().x, portal.getPosition().y, portal.getPosition().z);
		pPoseStack.mulPose(new Quaternionf(
				portal.getOrientation().x,
				portal.getOrientation().y,
				portal.getOrientation().z,
				portal.getOrientation().w
		).normalize());
	}
	
	protected void finishRender(PoseStack pPoseStack, BasicPortal portal) {
		pPoseStack.popPose();
		
		if (portal.isDoubleSided()) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
	}
	
	@Override
	public void drawStencil(MultiBufferSource.BufferSource source, PoseStack pPoseStack, Camera pCamera, BasicPortal portal, Tesselator tesselator) {
		setupRender(pPoseStack, portal);
		drawQuad(pPoseStack, portal.getSize().x / 2, portal.getSize().y / 2, tesselator);
		finishRender(pPoseStack, portal);
	}
	
	@Override
	public void drawOverlay(MultiBufferSource.BufferSource source, PoseStack pPoseStack, Camera pCamera, BasicPortal portal, Tesselator tesselator) {
	}
	
	@Override
	public void setupMatrix(BasicPortal portal, PoseStack stack) {
		// translate
		Vec3 position = portal.getPosition();
		stack.translate(position.x, position.y, position.z);
		// rotate
		Quaterniond quaternion = portal.getOrientation();
		if (portal.getConnectedNetwork().getPortals().size() == 1) quaternion.mul(new Quaterniond(0, 0, 0, 1).rotateAxis((float) Math.toRadians(-90), 0, 1, 0));
		stack.mulPose(new Quaternionf(
				quaternion.x,
				quaternion.y,
				quaternion.z,
				quaternion.w
		));
//		// adjust normals
//		quaternion.normalize();
//		stack.last().normal().mul(quaternion);
		
		float xScl = (float) portal.getSize().x;
		float yScl = (float) portal.getSize().y;
		stack.scale(xScl, yScl, xScl);
	}
	
	@Override
	public void setupAsTarget(BasicPortal portal, PoseStack stack) {
		float xScl = 1f / (float) portal.getSize().x;
		float yScl = 1f / (float) portal.getSize().y;
		
		stack.scale(xScl, yScl, xScl);
		
		boolean isMirror = portal.getConnectedNetwork().getPortals().size() == 1;
		Vec3 position = portal.getPosition();
		
		// rotate
		if (isMirror) {
			// mirror
			stack.scale(1, 1, -1);
			stack.last().normal().scale(1, 1, -1);
			// I don't really know why mirrors need this rotation
			Quaternionf quaternion = new Quaternionf(0, 0, 0, 1).rotateAxis((float) Math.toRadians(180), 0, 1, 0);
			stack.mulPose(quaternion);
//			// adjust normals
//			quaternion.normalize();
//			stack.last().normal().mul(quaternion);
		}
		
		Quaterniond quaternion = portal.getOrientation();
		stack.mulPose(new Quaternionf(
				quaternion.x,
				quaternion.y,
				quaternion.z,
				quaternion.w
		));
//		// adjust normals
//		quaternion.normalize();
//		stack.last().normal().mul(quaternion);
		
		// translate
		stack.translate(-position.x, -position.y, -position.z);
	}
}
