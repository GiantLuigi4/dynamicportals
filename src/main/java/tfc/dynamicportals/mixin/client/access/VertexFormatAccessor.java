package tfc.dynamicportals.mixin.client.access;

import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VertexFormat.class)
public interface VertexFormatAccessor {
	@Invoker("_setupBufferState")
	void directSetRenderState();
}
