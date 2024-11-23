package tfc.dynamicportals.mixin.client.render.optim.lookup;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.network.util.optim.ReferenceMap;

import java.util.Map;

@Mixin(ShaderInstance.class)
public abstract class QuickUniformLookup {
    @Mutable
    @Shadow
    @Final
    private Map<String, Object> samplerMap;

    @Mutable
    @Shadow
    @Final
    private Map<String, Uniform> uniformMap;

    @Shadow public abstract void markDirty();

    @Shadow private boolean dirty;
    @Unique
    private Object2IntMap<String> dynamicportals$locationMap;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V")
    public final void postInit(ResourceProvider pResourceProvider, ResourceLocation shaderLocation, VertexFormat pVertexFormat, CallbackInfo ci) {
        // put gets used on this, so wrap with reference map to avoid that
        samplerMap = new ReferenceMap<>(new Object2ObjectAVLTreeMap<>());
        // these maps shouldn't be modified much, so use a data structure meant for infrequent modifications
        uniformMap = new Object2ObjectAVLTreeMap<>();
        dynamicportals$locationMap = new Object2IntAVLTreeMap<>();
    }

    @Redirect(require = 0, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;glGetUniformLocation(ILjava/lang/CharSequence;)I"), method = "apply")
    public final int cacheLocations(int pProgram, CharSequence pName) {
        Integer i = dynamicportals$locationMap.get(pName.toString());
        if (i == null) {
            int loc = Uniform.glGetUniformLocation(pProgram, pName);
            dynamicportals$locationMap.put(pName.toString(), loc);
            return loc;
        }
        return i;
    }

    /**
     * @author GiantLuigi4
     * @reason inline markDirty
     */
    @Overwrite
    public void setSampler(String pName, Object pTextureId) {
        this.samplerMap.put(pName, pTextureId);
        this.dirty = true;
    }
}
