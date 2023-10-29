package tfc.dynamicportals.util.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.mixin.client.data.access.FrustumAccessor;

public class FrustumDrawer {
    private static void drawVec(VertexConsumer consumer, PoseStack pPoseStack, int col, Vector4f vec, Vector4f cam, Matrix4f proj) {
        vec = new Vector4f(vec.x(), vec.y(), vec.z(), vec.w());
        if (proj != null)
            vec.mul(10);
        if (cam == null)
            cam = new Vector4f();

        int r = col == 0 ? 255 : 0;
        int g = col == 1 ? 255 : 0;
        int b = col == 2 ? 255 : 0;
        if (col == 3) r = g = b = 255;

        consumer.vertex(
                pPoseStack.last().pose(),
                vec.x(),
                vec.y(),
                vec.z()
        ).color(r, g, b, 255).normal(pPoseStack.last().normal(), vec.x() - cam.x(), vec.y() - cam.y(), vec.z() - cam.z()).endVertex();
        consumer.vertex(
                pPoseStack.last().pose(),
                cam.x(),
                cam.y(),
                cam.z()
        ).color(r, g, b, 255).normal(pPoseStack.last().normal(), vec.x() - cam.x(), vec.y() - cam.y(), vec.z() - cam.z()).endVertex();
    }

    public static void draw(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, Frustum frustum) {
        pPoseStack.pushPose();

        FrustumAccessor accessor = ((FrustumAccessor) frustum);

        PoseStack pMatrixStack = new PoseStack();
        net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(pGameRenderer, pCamera, pPartialTick);
        pCamera.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));

        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(pCamera.getXRot()));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(pCamera.getYRot() + 180.0F));

        MiniFrustum frustum1 = new MiniFrustum(pMatrixStack.last().pose(), pProjectionMatrix);
        frustum1.calculateFrustum(new Vector4f(
                (float) (-16 - pCamera.getPosition().x),
                (float) (64 - pCamera.getPosition().y),
                (float) (-2 - pCamera.getPosition().z),
                0
        ), pPoseStack.last().pose(), pProjectionMatrix);
        frustum1.prepare(
                pCamera.getPosition().x,
                pCamera.getPosition().y,
                pCamera.getPosition().z
        );

        Vec3 pretend = new Vec3(-11, 64, 6);

        pPoseStack.translate(
                -pCamera.getPosition().x + pretend.x,
                -pCamera.getPosition().y + pretend.y,
                -pCamera.getPosition().z + pretend.z
        );

        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);

        Vec3 crd = new Vec3(
                -20,
                64,
                -6
        );

        drawVec(
                consumer,
                pPoseStack,
                frustum1.isVisible(
                        new AABB(
                                crd.x,
                                crd.y,
                                crd.z,
                                crd.x,
                                crd.y,
                                crd.z
                        )
                ) ? 3 : 4,
                new Vector4f(
                        -20.1f - (float) pretend.x,
                        64.1f - (float) pretend.y,
                        -6.1f - (float) pretend.z,
                        0
                ),
                new Vector4f(
                        -19.9f - (float) pretend.x,
                        63.9f - (float) pretend.y,
                        -5.9f - (float) pretend.z,
                        0
                ), null
        );

        pPoseStack.popPose();

        pPoseStack.pushPose();

        pPoseStack.translate(
                -pCamera.getPosition().x,
                -pCamera.getPosition().y,
                -pCamera.getPosition().z
        );
        pPoseStack.translate(-11, 63, -2);

        for (Vector4f frustumDatum : accessor.getFrustumData()) {
            drawVec(
                    consumer, pPoseStack,
                    1, frustumDatum,
                    new Vector4f(),
                    null
            );
        }
        for (Vector4f frustumDatum : ((FrustumAccessor) frustum1).getFrustumData()) {
            drawVec(
                    consumer, pPoseStack,
                    0, frustumDatum,
                    new Vector4f(),
                    null
            );
        }

        pPoseStack.popPose();

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
    }
}
