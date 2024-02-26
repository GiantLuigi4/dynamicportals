package tfc.dynamicportals.mixin.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.client.AbstractPortalRenderDispatcher;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.util.render.RenderUtil;

import javax.annotation.Nullable;
import java.util.ArrayDeque;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Nullable
    private ClientLevel level;
    @Shadow
    private boolean captureFrustum;
    @Shadow
    @Nullable
    public Frustum capturedFrustum;
    @Shadow
    private Frustum cullingFrustum;
    @Unique
    private static int recurse = 0;
    @Unique
    private static boolean allowRecurse = true;

    @Unique
    private static final int MAX_RECURSE = 4;
    
    @Inject(at = @At("HEAD"), method = "renderLevel")
    public void preDrawLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        recurse++;
        RenderUtil.activeLayer = recurse - 1;
    }

    @Inject(at = @At("RETURN"), method = "renderLevel")
    public void postDrawLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        recurse--;
        RenderUtil.activeLayer = recurse - 1;
    }

    private static final ArrayDeque<AbstractPortal> rendering = new ArrayDeque<>();
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V", ordinal = 3), method = "renderLevel")
    public void drawPortals(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        if (recurse <= MAX_RECURSE && allowRecurse) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
        
            AbstractPortalRenderDispatcher renderer = AbstractPortalRenderDispatcher.getSelected();
            renderer.push(recurse - 1);
            Tesselator tessel = Tesselator.getInstance();
            for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
                for (AbstractPortal portal : portalNetwork.getPortals()) {
                    if (portal.exitOnly()) continue;
                    
                    if (!rendering.isEmpty() && rendering.peek() == portal)
                        continue;
                    
                    if (portal.myLevel == level) {
                        rendering.push(portal);
                        
                        AbstractPortalRenderDispatcher dispatcher =
                                portal.preferredDispatcher() == null ?
                                        renderer :
                                        portal.preferredDispatcher()
                                ;
                        allowRecurse = dispatcher.supportsRecurse();
                        dispatcher.draw(tessel, minecraft, minecraft.renderBuffers().bufferSource(), pPoseStack, pProjectionMatrix, captureFrustum ? capturedFrustum : cullingFrustum, pCamera, portal, pGameRenderer, pPartialTick);
                        allowRecurse = true;
                        
                        renderer.pop(recurse - 1);
                        
                        rendering.pop();
                    }
                }
            }
            minecraft.renderBuffers().bufferSource().endBatch();
            
            RenderUtil.activeLayer = recurse - 1;
            
            if (recurse == 1) {
                GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
                
                GL11.glStencilMask(0xFF);
                GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
                GL11.glStencilMask(0x00);
                
                GL11.glDisable(GL11.GL_STENCIL_TEST);
            }
            
            // reset fog
            {
                float renderDist = pGameRenderer.getRenderDistance();
                Minecraft mc = this.minecraft;
                boolean foggy = mc.level.effects().isFoggyAt(Mth.floor(pCamera.getPosition().x), Mth.floor(pCamera.getPosition().z)) || mc.gui.getBossOverlay().shouldCreateWorldFog();
                FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(renderDist, 32.0F), foggy, pPartialTick);
                FogRenderer.setupColor(pCamera, pPartialTick, mc.level, mc.options.getEffectiveRenderDistance(), pGameRenderer.getDarkenWorldAmount(pPartialTick));
                FogRenderer.levelFogColor();
                
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
        }
        
        GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
