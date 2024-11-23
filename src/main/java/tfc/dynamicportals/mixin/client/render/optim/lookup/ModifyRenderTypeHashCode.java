package tfc.dynamicportals.mixin.client.render.optim.lookup;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderType.class)
public class ModifyRenderTypeHashCode {
    @Unique
    private static int __next_Index = 0;
    @Unique
    private int dynamicportals$index = 0;

    @Inject(at = @At("TAIL"), method = "<init>")
    public final void postInit(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState, CallbackInfo ci) {
        if (pFormat == DefaultVertexFormat.BLOCK) {
            dynamicportals$index = __next_Index++;
        } else dynamicportals$index = super.hashCode();
    }

    // create a "unique" hash code for each render type which takes next to no computation to evaluate
    // allows for maps to accelerated a bit more
    public int hashCode() {
        return dynamicportals$index;
    }
}
