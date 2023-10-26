package tfc.dynamicportals.mixin.client.data.access;

import net.minecraft.client.Camera;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// TEMPORARY
// Portals should have their own camera instances
@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor void setLevel(BlockGetter getter);
}
