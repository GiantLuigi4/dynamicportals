package tfc.dynamicportals.client.render.cull;

import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

// TODO
// goal: compute quad that represents a greedy overlap between the projected camera frustum and the portal quad
//       use this as the base of the frustum
//       then project that to a far plane
//       have the frustum pass between those two quads
//
// to define greedy overlap: in order to have a perfect overlap frustum between the portal and the player camera it may need 5 sides, which would be expensive
// so the frustum needs to remain as a quad, but if it's just bounded to the camera's corners, it may miss some areas
// so it needs to compute the intersection between the portal quad and the edge of the screen to create a more greedily allocated quad that contains all of the portal
public class AdvancedFrustum extends Frustum {
    public AdvancedFrustum(Matrix4f pProjection, Matrix4f pFrustrum) {
        super(pProjection, pFrustrum);
    }

    public AdvancedFrustum(Frustum pOther) {
        super(pOther);
    }

    @Override
    public Frustum offsetToFullyIncludeCameraCube(int p_194442_) {
        // don't care
        return this;
    }

    @Override
    public void prepare(double pCamX, double pCamY, double pCamZ) {
        super.prepare(pCamX, pCamY, pCamZ);
    }

    @Override
    public boolean isVisible(AABB pAabb) {
        return super.isVisible(pAabb);
    }
}
