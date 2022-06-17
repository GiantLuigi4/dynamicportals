package tfc.dynamicportals.api;

import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.LightTexture;

public class Portal extends AbstractPortal {
	@Override
	public void drawStencil(VertexConsumer builder, PoseStack stack) {
		Vector2d size = new Vector2d(10, 10);
		float r = 1, b = r, g = b, a = g;
		Matrix4f mat = stack.last().pose();
		// TODO: use a custom vertex builder which automatically fills in missing elements
		builder.vertex(mat, -((float) 10 / 2), 0, 0).color(1, 0, 0, a).uv(0,0).uv2(LightTexture.FULL_BRIGHT).endVertex();
		builder.vertex(mat, ((float) 10 / 2), 0, 0).color(0, 1, 0, a).uv(0,0).uv2(LightTexture.FULL_BRIGHT).endVertex();
		builder.vertex(mat, ((float) 10 / 2), (float) 10, 0).color(0, 0, 1, a).uv(0,0).uv2(LightTexture.FULL_BRIGHT).endVertex();
		builder.vertex(mat, -((float) 10 / 2), (float) 10, 0).color(r, 0, b, a).uv(0,0).uv2(LightTexture.FULL_BRIGHT).endVertex();
	}
}
