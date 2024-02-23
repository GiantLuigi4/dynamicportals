package tfc.dynamicportals.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

public class DypoShaders {
	
	protected static ShaderInstance DEPTH_CLEAR;
	
	public static void registerShaders(final RegisterShadersEvent event) {
		try {
			event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("dynamicportals", "depth_clear"), DefaultVertexFormat.BLOCK), (shader) -> DEPTH_CLEAR = shader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ShaderInstance getDepthClear() {
		return DEPTH_CLEAR;
	}
	
	public static void init(IEventBus bus) {
		bus.addListener(DypoShaders::registerShaders);
	}
}
