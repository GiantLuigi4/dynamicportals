package tfc.dynamicportals.mixin.core.collision;

import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;

@Mixin(Entity.class)
public abstract class EntityMoveMixin {
    @Shadow
    private Level level;

    @Shadow
    public abstract AABB getBoundingBox();

    @Inject(at = @At("HEAD"), method = "move")
    public void preMove(MoverType pType, Vec3 pPos, CallbackInfo ci) {
        ProfilerFiller profilerFiller = level.getProfiler();
        profilerFiller.push("teleportation_check");
        AABB box = getBoundingBox();
        Vector3d temp = new Vector3d();
        double bestDist = pPos.length();
        AbstractPortal bestPortal = null;

        for (PortalNet portalNetwork : ((NetworkHolder) level).getPortalNetworks()) {
            for (AbstractPortal portal : portalNetwork.getPortals()) {
                if (portal.myLevel != level) continue;
                if (portal.wasCrossed(box, pPos, temp)) {
                    double dist = portal.distanceAlong(box, pPos, temp);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestPortal = portal;
                    }
                }
            }
        }

        if (bestPortal != null) {
            bestPortal.teleport((Entity) (Object) this);
        }

        profilerFiller.pop();
    }
}
