package tfc.dynamicportals.util.debug;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.culling.Frustum;
import tfc.dynamicportals.mixin.client.data.access.FrustumAccessor;

public class MiniFrustum extends Frustum {
    public MiniFrustum(Matrix4f pProjection, Matrix4f pFrustrum) {
        super(pProjection, pFrustrum);
    }

    public MiniFrustum(Frustum pOther) {
        super(pOther);
    }

    public void calculateFrustum(Vector4f portal, Matrix4f pProjection, Matrix4f pFrustrumMatrix) {
        Matrix4f matrix4f = pFrustrumMatrix.copy();
        matrix4f.multiply(pProjection);
        matrix4f.transpose();
        portal.set(0, 0, 0, 0);

        ((FrustumAccessor) this).setViewVector(new Vector4f(0, 0, 1, 0.0F));
        ((FrustumAccessor) this).getViewVector().transform(matrix4f);
        this.getPlane(matrix4f, portal.x() - 1, portal.y(), portal.z(), 0);
        this.getPlane(matrix4f, portal.x() + 1, portal.y(), portal.z(), 1);
        this.getPlane(matrix4f, portal.x(), portal.y() - 1, portal.z(), 2);
        this.getPlane(matrix4f, portal.x(), portal.y() + 1, portal.z(), 3);
        this.getPlane(matrix4f, portal.x(), portal.y(), -1, 4);
        this.getPlane(matrix4f, portal.x(), portal.y(), 1, 5);
    }

    private void getPlane(Matrix4f pFrustrumMatrix, float pX, float pY, float pZ, int pId) {
        Vector4f vector4f = new Vector4f(pX, pY, pZ, 1.0F);
        if (pId == 0) {
            vector4f.set(
                    vector4f.x() * 2 ,
                    vector4f.y() * 2,
                    vector4f.z() * 2,
                    vector4f.w()
            );
        } else if (pId < 4) {
            vector4f.set(
                    vector4f.x() * 2,
                    vector4f.y() * 2,
                    vector4f.z() * 2,
                    vector4f.w()
            );
        }
        vector4f.transform(pFrustrumMatrix);
        vector4f.normalize();
        ((FrustumAccessor) this).getFrustumData()[pId] = vector4f;
    }
}
