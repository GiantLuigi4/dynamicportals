package tfc.dynamicportals.client.render.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.itf.RendererHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class NetworkDrawer {
    public static void drawLine(
            VertexConsumer consumer,
            PoseStack stack,
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double r, double g, double b
    ) {
        consumer
                .vertex(stack.last().pose(), (float) x0, (float) y0, (float) z0)
                .color((float) r, (float) g, (float) b, 1)
                .normal(stack.last().normal(), (float) (x1 - x0), (float) (y1 - y0), (float) (z1 - z0))
                .endVertex();
        consumer
                .vertex(stack.last().pose(), (float) x1, (float) y1, (float) z1)
                .color((float) r, (float) g, (float) b, 1)
                .normal(stack.last().normal(), (float) (x1 - x0), (float) (y1 - y0), (float) (z1 - z0))
                .endVertex();
    }

    public static void debugDraw(Minecraft minecraft, Level level, PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;

        MultiBufferSource.BufferSource source = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = source.getBuffer(RenderType.LINES);

        Random rng = new Random(98432);
        for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
            List<AbstractPortal> portals = portalNetwork.getPortals();
            HashMap<AbstractPortal, Long> seeds = new HashMap<>();
            for (int i = 0; i < portals.size() - 1; i++) {
                AbstractPortal thisPortal = portals.get(i);
                AbstractPortal nextPortal = portals.get(i + 1);
                Random colGen = new Random(seeds.computeIfAbsent(thisPortal, (p) -> rng.nextLong()));
                consumer
                        .vertex(
                                pPoseStack.last().pose(),
                                (float) (thisPortal.getPosition().x - pCamera.getPosition().x),
                                (float) (thisPortal.getPosition().y - pCamera.getPosition().y),
                                (float) (thisPortal.getPosition().z - pCamera.getPosition().z)
                        )
                        .color(colGen.nextInt(), colGen.nextInt(), colGen.nextInt(), 255)
                        .normal(pPoseStack.last().normal(), (float) (thisPortal.getPosition().x - nextPortal.getPosition().x), (float) (thisPortal.getPosition().y - nextPortal.getPosition().y), (float) (thisPortal.getPosition().z - nextPortal.getPosition().z))
                        .endVertex();
                colGen = new Random(seeds.computeIfAbsent(nextPortal, (p) -> rng.nextLong()));
                consumer
                        .vertex(
                                pPoseStack.last().pose(),
                                (float) (nextPortal.getPosition().x - pCamera.getPosition().x),
                                (float) (nextPortal.getPosition().y - pCamera.getPosition().y),
                                (float) (nextPortal.getPosition().z - pCamera.getPosition().z)
                        )
                        .color(colGen.nextInt(), colGen.nextInt(), colGen.nextInt(), 255)
                        .normal(pPoseStack.last().normal(), (float) (thisPortal.getPosition().x - nextPortal.getPosition().x), (float) (thisPortal.getPosition().y - nextPortal.getPosition().y), (float) (thisPortal.getPosition().z - nextPortal.getPosition().z))
                        .endVertex();
            }
        }

        source.endBatch();
        consumer = source.getBuffer(RenderType.LINES);
        for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
            for (AbstractPortal portal : portalNetwork.getPortals()) {
                if (portal.myLevel != level) {
                    double r = portal.myLevel == null ? 1 : 0;
                    double g = 1 - r;

                    drawLine(
                            consumer, pPoseStack,
                            portal.getPosition().x - pCamera.getPosition().x, portal.getPosition().y - pCamera.getPosition().y, portal.getPosition().z - pCamera.getPosition().z,
                            portal.getPosition().x - pCamera.getPosition().x, portal.getPosition().y - pCamera.getPosition().y + 0.1, portal.getPosition().z - pCamera.getPosition().z,
                            r, 0, g
                    );
                    drawLine(
                            consumer, pPoseStack,
                            portal.getPosition().x - pCamera.getPosition().x, portal.getPosition().y - pCamera.getPosition().y + 0.3, portal.getPosition().z - pCamera.getPosition().z,
                            portal.getPosition().x - pCamera.getPosition().x, portal.getPosition().y - pCamera.getPosition().y + 1, portal.getPosition().z - pCamera.getPosition().z,
                            r, 0, g
                    );
                } else {
                    ((RendererHolder) portal.type).getRenderer().drawAntenna(
                            portal, consumer, pPoseStack, pCamera
                    );
                }
            }
        }

        source.endBatch();

        pPoseStack.pushPose();
        pPoseStack.translate(
                -pCamera.getPosition().x,
                -pCamera.getPosition().y,
                -pCamera.getPosition().z
        );
        for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
            for (AbstractPortal portal : portalNetwork.getPortals()) {
                if (portal.myLevel == level) {
                    ((RendererHolder) portal.type).getRenderer().drawDebug(
                            portal,
                            source,
                            pPoseStack,
                            pCamera
                    );
                }
            }
        }
        pPoseStack.popPose();

        source.endBatch();
    }
}
