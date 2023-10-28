package tfc.dynamicportals.util.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
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

        Vec3 pretend = new Vec3(-11, 64, 6);
//        Vec3 pretend = pCamera.getPosition();

        pPoseStack.translate(
                -pCamera.getPosition().x + pretend.x,
                -pCamera.getPosition().y + pretend.y,
                -pCamera.getPosition().z + pretend.z
        );
        Matrix4f proj = RenderSystem.getProjectionMatrix();

        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);

        drawVec(
                consumer, pPoseStack,
                0,
                new Vector4f(
                        -16 - (float) pretend.x,
                        64 - (float) pretend.y,
                        -2 - (float) pretend.z,
                        0
                ), null, null
        );

        boolean pass = true;

        Vec3 crd = new Vec3(
                -20,
                64,
                -6
        );

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) continue;

                drawVec(
                        consumer, pPoseStack,
                        2,
                        new Vector4f(
                                -16 - (float) pretend.x,
                                (64 + y) - (float) pretend.y,
                                (-2 + x) - (float) pretend.z,
                                0
                        ), null, new Matrix4f()
                );
            }
        }

        drawVec(
                consumer,
                pPoseStack,
                pass ? 3 : 4,
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

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        pPoseStack.popPose();
    }
}
