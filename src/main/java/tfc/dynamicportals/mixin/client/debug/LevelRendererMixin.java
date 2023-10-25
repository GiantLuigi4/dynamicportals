package tfc.dynamicportals.mixin.client.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.Random;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0), method = "renderLevel")
    public void debugDraw(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        MultiBufferSource.BufferSource source = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = source.getBuffer(RenderType.LINE_STRIP);

        Random rng = new Random(98432);
        for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
            for (AbstractPortal portal : portalNetwork.getPortals()) {
                consumer
                        .vertex(pPoseStack.last().pose(), (float) portal.position.x, (float) portal.position.y, (float) portal.position.z)
                        .color(rng.nextInt(), rng.nextInt(), rng.nextInt(), 255)
                        .normal(1, 0, 0)
                ;
            }
        }
        source.getBuffer(RenderType.solid());
    }
}
