package tfc.dynamicportals.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import tfc.dynamicportals.Renderer;

import java.util.concurrent.atomic.AtomicReference;

public class RenderTypes {
	public static final AtomicReference<RenderTarget> targ = new AtomicReference<>();
	private static final RenderStateShard.ShaderStateShard RENDERTYPE_LEASH_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader);
	public static final RenderType STENCIL_DRAW = RenderType.create(
			"dynamic_portals_stencil",
			DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.QUADS,
			256,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LEASH_SHADER)
					.setTextureState(RenderType.NO_TEXTURE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false)
	);
	private static final RenderStateShard.ShaderStateShard POSITION_COLOR_TEX = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexShader);
	private static final RenderStateShard.EmptyTextureStateShard FBOTexture = new RenderStateShard.EmptyTextureStateShard(() -> {
		RenderSystem.enableTexture();
		RenderSystem.setShaderTexture(0, targ.get().getColorTextureId());
	}, () -> {
	});
	public static final RenderType END_PORTAL_FRAME = RenderType.create(
			"dynamic_portals_stencil",
			DefaultVertexFormat.POSITION_COLOR_TEX,
			VertexFormat.Mode.QUADS,
			256,
			RenderType.CompositeState.builder()
					.setShaderState(POSITION_COLOR_TEX)
					.setTextureState(FBOTexture)
					.setCullState(RenderType.NO_CULL)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false)
	);
	
	
	public static void update() {
		if (targ.get() == null)
			targ.set(new TextureTarget(1, 1, false, Minecraft.ON_OSX));
		RenderTarget targ = RenderTypes.targ.get();
		if (Renderer.fboWidth() != targ.width || Renderer.fboHeight() != targ.height)
			targ.resize((int) Renderer.fboWidth(), (int) Renderer.fboHeight(), Minecraft.ON_OSX);
	}
}
