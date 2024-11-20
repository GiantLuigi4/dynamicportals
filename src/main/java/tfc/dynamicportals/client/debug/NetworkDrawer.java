package tfc.dynamicportals.client.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.ClientPortalType;
import tfc.dynamicportals.itf.NetworkHolder;

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

    private static long seed(AbstractPortal portal, HashMap<AbstractPortal, Long> seeds, Random rng) {
        Long lng = seeds.get(portal);
        if (lng == null) seeds.put(portal, lng = rng.nextLong());
        return lng;
    }

    public static void debugDraw(Minecraft minecraft, Level level, PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;

        MultiBufferSource.BufferSource source = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = source.getBuffer(RenderType.LINES);

        pPoseStack.pushPose();
        pPoseStack.translate(
                -pCamera.getPosition().x,
                -pCamera.getPosition().y,
                -pCamera.getPosition().z
        );
        Random rng = new Random(98432);
        // first pass: runs through all but the last portal
        for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
            List<AbstractPortal> portals = portalNetwork.getPortals();
            HashMap<AbstractPortal, Long> seeds = new HashMap<>();
            for (int i = 0; i < portals.size() - 1; i++) {
                AbstractPortal thisPortal = portals.get(i);
                AbstractPortal nextPortal = portals.get(i + 1);
                Random colGen = new Random(seed(thisPortal, seeds, rng));
                consumer
                        .vertex(
                                pPoseStack.last().pose(),
                                (float) (thisPortal.getPosition().x),
                                (float) (thisPortal.getPosition().y),
                                (float) (thisPortal.getPosition().z)
                        )
                        .color(colGen.nextInt(), colGen.nextInt(), colGen.nextInt(), 255)
                        .normal(pPoseStack.last().normal(), (float) (thisPortal.getPosition().x - nextPortal.getPosition().x), (float) (thisPortal.getPosition().y - nextPortal.getPosition().y), (float) (thisPortal.getPosition().z - nextPortal.getPosition().z))
                        .endVertex();
                colGen = new Random(seed(nextPortal, seeds, rng));
                consumer
                        .vertex(
                                pPoseStack.last().pose(),
                                (float) (nextPortal.getPosition().x),
                                (float) (nextPortal.getPosition().y),
                                (float) (nextPortal.getPosition().z)
                        )
                        .color(colGen.nextInt(), colGen.nextInt(), colGen.nextInt(), 255)
                        .normal(pPoseStack.last().normal(), (float) (thisPortal.getPosition().x - nextPortal.getPosition().x), (float) (thisPortal.getPosition().y - nextPortal.getPosition().y), (float) (thisPortal.getPosition().z - nextPortal.getPosition().z))
                        .endVertex();
            }
        }

        // new pass: runs the full length
        for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
            for (AbstractPortal portal : portalNetwork.getPortals()) {
                if (portal.myLevel != level) {
                    double r = portal.myLevel == null ? 1 : 0;
                    double g = 1 - r;

                    drawLine(
                            consumer, pPoseStack,
                            portal.getPosition().x, portal.getPosition().y, portal.getPosition().z,
                            portal.getPosition().x, portal.getPosition().y + 0.1, portal.getPosition().z,
                            r, 0, g
                    );
                    drawLine(
                            consumer, pPoseStack,
                            portal.getPosition().x, portal.getPosition().y + 0.3, portal.getPosition().z,
                            portal.getPosition().x, portal.getPosition().y + 1, portal.getPosition().z,
                            r, 0, g
                    );
                } else {
                    ((ClientPortalType) portal.type).getRenderer().drawAntenna(
                            portal, consumer, pPoseStack, pCamera
                    );
                }
            }
        }

        // to make sure everything above stays in one batch, this is going down here
        // why? because this has access to the buffer source, so it can start a new batch
        for (PortalNet portalNetwork : ((NetworkHolder) minecraft).getPortalNetworks()) {
            for (AbstractPortal portal : portalNetwork.getPortals()) {
                if (portal.myLevel == level) {
                    ((ClientPortalType) portal.type).getRenderer().drawDebug(
                            portal,
                            source,
                            pPoseStack,
                            pCamera
                    );
                }
            }
        }

        source.endBatch();

        pPoseStack.popPose();
    }
}
