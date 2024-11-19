package tfc.dynamicportals.client.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import tfc.dynamicportals.mixin.client.access.FrustumAccessor;

import java.lang.reflect.Field;

public class FrustumDrawer {
    private static void drawVec(VertexConsumer consumer, PoseStack pPoseStack, int col, Vector4f vec, Vector4f cam, Matrix4f proj) {
        vec = new Vector4f(vec.x(), vec.y(), vec.z(), vec.w());
//        if (proj != null)
//            vec.mul(10);
        if (cam == null)
            cam = new Vector4f();

        int r = ((col & 1) == 1) ? 255 : 0;
        int g = ((col & 2) == 2) ? 255 : 0;
        int b = ((col & 4) == 4) ? 255 : 0;
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

    // compute corners of frustum from frustum data
    // ngl, I do not know why this works, I asked claude ai to take Frustum#getPlane and extract the corners, then cleaned up and fixed the code
    protected static Vector4f intersect(Vector4f p1, Vector4f p2, Vector4f p3) {
        Vector3f n1 = new Vector3f(p1.x(), p1.y(), p1.z());
        Vector3f n2 = new Vector3f(p2.x(), p2.y(), p2.z());
        Vector3f n3 = new Vector3f(p3.x(), p3.y(), p3.z());

        // compute determinate
        Vector3f temp = new Vector3f(n2);
        temp.cross(n3);
        float det = n1.dot(temp);

        Vector3f result = new Vector3f();
        // w = negative distance from origin (according to claude)
        // p3.w(n1 X n2)
        temp.set(n1.x(), n1.y(), n1.z());
        temp.cross(n2);
        temp.mul(-p3.w());
        result.add(temp);

        // p1.w(n2 X n3)
        temp.set(n2.x(), n2.y(), n2.z());
        temp.cross(n3);
        temp.mul(-p1.w());
        result.add(temp);

        // p2.w(n3 X n1)
        temp.set(n3.x(), n3.y(), n3.z());
        temp.cross(n1);
        temp.mul(-p2.w());
        result.add(temp);

        result.mul(1.f / det);
        return new Vector4f(result.x(), result.y(), result.z(), 0.0f);
    }

    static Frustum capture;
    static Vec3 pretend = new Vec3(-11, 64, 6);

    public static void draw(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, Frustum frustum) {
        if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;

        if (!Minecraft.getInstance().options.f_92063_) {
            capture = null;
            pretend = pCamera.getPosition();
            return;
        }

        pPoseStack.pushPose();

        if (capture == null)
            capture = frustum;

        pPoseStack.translate(
                -pCamera.getPosition().x + pretend.x,
                -pCamera.getPosition().y + pretend.y,
                -pCamera.getPosition().z + pretend.z
        );

        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);

        FrustumAccessor accessor = (FrustumAccessor) capture;

        Vector4f[] corners = new Vector4f[4];
        FrustumIntersection intersection = accessor.getIntersection();
        Vector4f[] frustumPlanes;
        try {
            Field planes = FrustumIntersection.class.getDeclaredField("planes");
            planes.setAccessible(true);
            frustumPlanes = (Vector4f[]) planes.get(intersection);
        } catch (Throwable err) {
            throw new RuntimeException(err);
        }

        // near
//        corners[0] = intersect(frustumPlanes[5], frustumPlanes[1], frustumPlanes[3]);
//        corners[1] = intersect(frustumPlanes[5], frustumPlanes[0], frustumPlanes[3]);
//        corners[2] = intersect(frustumPlanes[5], frustumPlanes[0], frustumPlanes[2]);
//        corners[3] = intersect(frustumPlanes[5], frustumPlanes[1], frustumPlanes[2]);

        // far
        corners[0] = intersect(frustumPlanes[4], frustumPlanes[1], frustumPlanes[3]);
        corners[1] = intersect(frustumPlanes[4], frustumPlanes[0], frustumPlanes[3]);
        corners[2] = intersect(frustumPlanes[4], frustumPlanes[0], frustumPlanes[2]);
        corners[3] = intersect(frustumPlanes[4], frustumPlanes[1], frustumPlanes[2]);

        int x = 0;
        int y = 0;
        int z = 0;
        for (int i = 0; i < corners.length; i++) {
            if (x == 1) {
                if (y == 1)
                    z = 1 - z;
                y = 1 - y;
            }
            x = 1 - x;
            int prev = i - 1;
            if (prev == -1) prev = corners.length - 1;
            drawVec(
                    consumer, pPoseStack, x << 2 | y << 1 | z,
                    corners[prev],
                    corners[i], pProjectionMatrix
            );
            drawVec(
                    consumer, pPoseStack, x << 2 | y << 1 | z,
                    corners[i],
                    null, pProjectionMatrix
            );
        }

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        pPoseStack.popPose();
    }
}
