package tfc.dynamicportals.client.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import tfc.dynamicportals.api.AbstractPortal;

import static tfc.dynamicportals.client.render.debug.NetworkDrawer.drawLine;

public abstract class AbstractPortalRenderer<T extends AbstractPortal> {
    public void drawDebug(
            T portal,
            MultiBufferSource.BufferSource source,
            PoseStack pPoseStack,
            Camera pCamera
    ) {
    }

    public void drawAntenna(T portal, VertexConsumer consumer, PoseStack pPoseStack, Camera pCamera) {
        drawLine(
                consumer, pPoseStack,
                portal.getPosition().x - pCamera.getPosition().x, portal.getPosition().y - pCamera.getPosition().y, portal.getPosition().z - pCamera.getPosition().z,
                portal.getPosition().x - pCamera.getPosition().x, portal.getPosition().y - pCamera.getPosition().y + 1, portal.getPosition().z - pCamera.getPosition().z,
                1, 1, 1
        );
        drawLine(
                consumer, pPoseStack,
                portal.getPosition().x - pCamera.getPosition().x, portal.getPosition().y - pCamera.getPosition().y + 1, portal.getPosition().z - pCamera.getPosition().z,
                portal.getPosition().x - pCamera.getPosition().x, portal.getPosition().y - pCamera.getPosition().y + 1.1, portal.getPosition().z - pCamera.getPosition().z,
                0, 1, 0
        );
    }
}
