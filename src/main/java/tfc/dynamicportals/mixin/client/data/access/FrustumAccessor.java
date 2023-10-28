package tfc.dynamicportals.mixin.client.data.access;

import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Frustum.class)
public interface FrustumAccessor {
    @Accessor Vector4f[] getFrustumData();
    @Accessor Vector4f getViewVector();
}
