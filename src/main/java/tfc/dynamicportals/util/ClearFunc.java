package tfc.dynamicportals.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;

public class ClearFunc {
    // TODO: figure this out
    public static void clear(int pMask) {
        if (true) return;

        boolean depthWrite;
        depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        if ((pMask & GL11.GL_DEPTH_BUFFER_BIT) == GL11.GL_DEPTH_BUFFER_BIT) {
            GL11.glDepthMask(true);
        } else {
            GL11.glDepthMask(false);
        }

        ShaderInstance sdr = RenderSystem.getShader();
        PoseStack rsStack = RenderSystem.getModelViewStack();
        RenderSystem.modelViewStack = new PoseStack();
        Matrix4f proj = RenderSystem.getProjectionMatrix();
        Matrix4f iProj = new Matrix4f();
        iProj.setIdentity();
        RenderSystem.setProjectionMatrix(iProj);
        RenderSystem.setShader(DypoShaders::getDepthClear);
        DypoShaders.getDepthClear().apply();

        int ofunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);

        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        float[] rsCol = RenderSystem.getShaderColor();
        rsCol = new float[]{rsCol[0], rsCol[1], rsCol[2], rsCol[3]};
        if ((pMask & GL11.GL_COLOR_BUFFER_BIT) == GL11.GL_COLOR_BUFFER_BIT) {
            float[] bkColor = new float[4];
            GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, bkColor);
            RenderSystem.setShaderColor(bkColor[0], bkColor[1], bkColor[2], bkColor[3]);
        }
        {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            builder.vertex(
                    -1, -1, 1
            ).endVertex();
            builder.vertex(
                    -1, 1, 1
            ).endVertex();
            builder.vertex(
                    1, 1, 1
            ).endVertex();
            builder.vertex(
                    1, -1, 1
            ).endVertex();
            tesselator.end();
        }
        DypoShaders.getDepthClear().clear();
        RenderSystem.setProjectionMatrix(proj);
        RenderSystem.setShaderColor(rsCol[0], rsCol[1], rsCol[2], rsCol[3]);
        RenderSystem.setShader(() -> sdr);
        RenderSystem.depthFunc(ofunc);
        RenderSystem.modelViewStack = rsStack;
        sdr.apply();

        if ((pMask & GL11.GL_DEPTH_BUFFER_BIT) == GL11.GL_DEPTH_BUFFER_BIT) {
            GL11.glDepthMask(depthWrite);
        }
    }
}
