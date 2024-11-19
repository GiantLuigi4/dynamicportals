package tfc.dynamicportals.mixin.client.access;

import net.minecraft.client.renderer.culling.Frustum;
import org.joml.FrustumIntersection;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Frustum.class)
public interface FrustumAccessor {
    @Accessor
    FrustumIntersection getIntersection();
}
