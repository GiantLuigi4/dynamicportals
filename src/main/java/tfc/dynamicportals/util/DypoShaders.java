package tfc.dynamicportals.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.io.IOException;

public class DypoShaders {
	
	protected static ShaderInstance DEPTH_CLEAR;
	
	public static void registerShaders(final RegisterShadersEvent event) {
		try {
			event.registerShader(new ShaderInstance(event.getResourceManager(), new ResourceLocation("dynamicportals", "depth_clear"), DefaultVertexFormat.BLOCK), (shader) -> DEPTH_CLEAR = shader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ShaderInstance getDepthClear() {
		return DEPTH_CLEAR;
	}
	
	static {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(DypoShaders::registerShaders);
	}
	
	public static void init() {
	}
}
