package tfc.dynamicportals.client.renderer;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.PortalType;

import java.lang.Math;

import static tfc.dynamicportals.client.debug.NetworkDrawer.drawLine;

public class BasicPortalRenderer extends AbstractPortalRenderer<BasicPortal> {
    public BasicPortalRenderer(PortalType<BasicPortal> type) {
        super(type);
    }

    private static final Quaternionf n90Y = new Quaternionf(0, 0, 0, 1).rotateAxis((float) Math.toRadians(-90), 0, 1, 0);

    protected void setupRender(PoseStack pPoseStack, BasicPortal portal) {
        if (portal.isDoubleSided()) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        } else {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }

        pPoseStack.pushPose();
        Vec3 pos = portal.getPosition();
        pPoseStack.translate(pos.x, pos.y, pos.z);
        Quaterniond qd = portal.getOrientation();
        pPoseStack.mulPose(new Quaternionf(qd.x, qd.y, qd.z, qd.w).normalize());
        pPoseStack.mulPose(n90Y);
    }

    protected void finishRender(PoseStack pPoseStack, BasicPortal portal) {
        pPoseStack.popPose();

        if (portal.isDoubleSided()) {
            GL11.glEnable(GL11.GL_CULL_FACE);
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
        Quaterniond quaternion = new Quaterniond(portal.getOrientation());
        if (portal.getConnectedNetwork().getPortals().size() == 1)
            quaternion.mul(new Quaterniond(0, 0, 0, 1).rotateAxis((float) Math.toRadians(-90), 0, 1, 0));
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

        Quaterniond quaternion = new Quaterniond(portal.getOrientation());
        quaternion.rotateAxis((float) Math.toRadians(180), 0, 1, 0);
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


    @Override
    public void drawDebug(
            BasicPortal portal,
            MultiBufferSource.BufferSource source,
            PoseStack pPoseStack, Camera pCamera
    ) {
        pPoseStack.pushPose();
        VertexConsumer consumer = source.getBuffer(RenderType.LINES);
        pPoseStack.translate(
                portal.getPosition().x,
                portal.getPosition().y,
                portal.getPosition().z
        );
        pPoseStack.mulPose(new Quaternionf(
                portal.getOrientation().x,
                portal.getOrientation().y,
                portal.getOrientation().z,
                portal.getOrientation().w
        ));

        Matrix4f pos = pPoseStack.last().pose();
        Matrix3f nor = pPoseStack.last().normal();
        line(
                consumer,
                pos, nor,
                new Vector3f(0, (float) -portal.getSize().y / 2f, (float) portal.getSize().x / 2f),
                new Vector3f(0, (float) portal.getSize().y / 2f, (float) portal.getSize().x / 2f),
                1, 0, 1, 1
        );
        line(
                consumer,
                pos, nor,
                new Vector3f(0, (float) portal.getSize().y / 2f, (float) portal.getSize().x / 2f),
                new Vector3f(0, (float) portal.getSize().y / 2f, (float) -portal.getSize().x / 2f),
                1, 0, 1, 1
        );
        line(
                consumer,
                pos, nor,
                new Vector3f(0, (float) portal.getSize().y / 2f, (float) -portal.getSize().x / 2f),
                new Vector3f(0, (float) -portal.getSize().y / 2f, (float) -portal.getSize().x / 2f),
                1, 0, 1, 1
        );
        line(
                consumer,
                pos, nor,
                new Vector3f(0, (float) -portal.getSize().y / 2f, (float) -portal.getSize().x / 2f),
                new Vector3f(0, (float) -portal.getSize().y / 2f, (float) portal.getSize().x / 2f),
                1, 0, 1, 1
        );

        // red = normal = forward
        line(
                consumer,
                pos, nor,
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                1, 0, 0, 1
        );
        // blue = right
        line(
                consumer,
                pos, nor,
                new Vector3f(0, 0, 0),
                new Vector3f(0, 0, 1),
                0, 0, 1, 1
        );
        // I would also have a green up vector, but that would conflict with another renderer
        // so white is up

        pPoseStack.popPose();
    }

    @Override
    public void drawAntenna(BasicPortal portal, VertexConsumer consumer, PoseStack pPoseStack, Camera pCamera) {
        pPoseStack.pushPose();
        pPoseStack.mulPose(new Quaternionf(
                portal.getOrientation().x,
                portal.getOrientation().y,
                portal.getOrientation().z,
                portal.getOrientation().w
        ));

        drawLine(
                consumer, pPoseStack,
                0, 0, 0,
                0, portal.getSize().y / 2f, 0,
                1, 1, 1
        );
        drawLine(
                consumer, pPoseStack,
                0, portal.getSize().y / 2f, 0,
                0, portal.getSize().y / 2f + 0.1f, 0,
                0, 1, 0
        );

        pPoseStack.popPose();
    }

    protected void line(
            VertexConsumer consumer,
            Matrix4f pos, Matrix3f nor,
            Vector3f from, Vector3f to,
            float r, float g, float b, float a
    ) {
        consumer.vertex(
                pos,
                from.x(),
                from.y(),
                from.z()
        ).color(r, g, b, a).normal(
                nor,
                to.x() - from.x(),
                to.y() - from.y(),
                to.z() - from.z()
        ).endVertex();
        consumer.vertex(
                pos,
                to.x(),
                to.y(),
                to.z()
        ).color(r, g, b, a).normal(
                nor,
                to.x() - from.x(),
                to.y() - from.y(),
                to.z() - from.z()
        ).endVertex();
    }
}
