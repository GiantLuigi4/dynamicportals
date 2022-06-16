package tfc.dynamicportals;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.dynamicportals.api.Renderer;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("dynamicportals")
public class DynamicPortals {
	
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	public DynamicPortals() {
//		FMLJavaModLoadingContext.get().getModEventBus().addListener();
//		MinecraftForge.EVENT_BUS.addListener();
		
		MinecraftForge.EVENT_BUS.addListener(Renderer::onRenderEvent);
		MinecraftForge.EVENT_BUS.addListener(Renderer::onBeginFrame);
	}
}
