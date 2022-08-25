package tfc.dynamicportals;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.dynamicportals.api.DynamicPortalsConfig;
import tfc.dynamicportals.command.DynamicPortalsCommand;
import tfc.dynamicportals.util.support.PehkuiSupport;

@Mod("dynamicportals")
public class DynamicPortals {
	// lorenzo: who needs LogManager.getLogger() when you have System.out :sunglasses:
	//private static final Logger LOGGER = LogManager.getLogger();
	
	public DynamicPortals() {
		MinecraftForge.EVENT_BUS.register(DynamicPortalsConfig.class);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DynamicPortalsConfig.clientSpec);
		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.addListener(Renderer::onRenderEvent);
		}
		MinecraftForge.EVENT_BUS.addListener(DynamicPortals::registerCommands);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	public static void registerCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(DynamicPortalsCommand.build(event.getDispatcher()));
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		if (ModList.get().isLoaded("pehkui")) PehkuiSupport.setup();
	}
}
