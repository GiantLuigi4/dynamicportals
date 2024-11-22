package tfc.dynamicportals.mixin.client.render.optim;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderType.class)
public class RenderTypeMixin {
    private static int __next_Index = 0;
    @Unique
    private int index = 0;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void postInit(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState, CallbackInfo ci) {
        index = __next_Index++;
    }

    // create a "unique" hash code for each render type which takes next to no computation to evaluate
    // allows for maps to accelerated a bit more
    public int hashCode() {
        return index;
    }
}
