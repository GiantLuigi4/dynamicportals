package tfc.dynamicportals.api;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public abstract class AbstractPortal {
	public abstract void drawStencil(VertexConsumer builder, PoseStack stack);
}
