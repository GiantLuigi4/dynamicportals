package tfc.dynamicportals.client.api.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.client.api.AbstractPortalRenderer;

import static tfc.dynamicportals.client.render.debug.NetworkDrawer.drawLine;

public class BasicPortalRenderer extends AbstractPortalRenderer<BasicPortal> {
    public BasicPortalRenderer() {
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
        pPoseStack.mulPose(portal.getOrientation());

        Matrix4f pos = pPoseStack.last().pose();
        Matrix3f nor = pPoseStack.last().normal();
        line(
                consumer,
                pos, nor,
                new Vector3f(0, -portal.getSize().y / 2f, portal.getSize().x / 2f),
                new Vector3f(0, portal.getSize().y / 2f, portal.getSize().x / 2f),
                1, 0, 1, 1
        );
        line(
                consumer,
                pos, nor,
                new Vector3f(0, portal.getSize().y / 2f, portal.getSize().x / 2f),
                new Vector3f(0, portal.getSize().y / 2f, -portal.getSize().x / 2f),
                1, 0, 1, 1
        );
        line(
                consumer,
                pos, nor,
                new Vector3f(0, portal.getSize().y / 2f, -portal.getSize().x / 2f),
                new Vector3f(0, -portal.getSize().y / 2f, -portal.getSize().x / 2f),
                1, 0, 1, 1
        );
        line(
                consumer,
                pos, nor,
                new Vector3f(0, -portal.getSize().y / 2f, -portal.getSize().x / 2f),
                new Vector3f(0, -portal.getSize().y / 2f, portal.getSize().x / 2f),
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
        pPoseStack.translate(
                portal.getPosition().x - pCamera.getPosition().x,
                portal.getPosition().y - pCamera.getPosition().y,
                portal.getPosition().z - pCamera.getPosition().z
        );
        pPoseStack.mulPose(portal.getOrientation());

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
}
