package tfc.dynamicportals.mixin.client.render.optim;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ShaderInstance.class)
public class QuickUniforms {
    @Mutable
    @Shadow
    @Final
    private Map<String, Object> samplerMap;

    @Mutable
    @Shadow
    @Final
    private Map<String, Uniform> uniformMap;

    // these maps shouldn't be modified much, so use a data structure meant for infrequent modifications
    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V")
    public void postInit(ResourceProvider pResourceProvider, ResourceLocation shaderLocation, VertexFormat pVertexFormat, CallbackInfo ci) {
        samplerMap = new Object2ObjectAVLTreeMap<>();
        uniformMap = new Object2ObjectAVLTreeMap<>();
    }
}
