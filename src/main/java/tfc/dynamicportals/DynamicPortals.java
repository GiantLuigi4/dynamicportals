package tfc.dynamicportals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.DynamicPortalsConfig;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.command.DynamicPortalsCommand;
import tfc.dynamicportals.networking.DynamicPortalsNetworkRegistry;
import tfc.dynamicportals.networking.sync.PortalUpdatePacket;
import tfc.dynamicportals.util.support.PehkuiSupport;

@Mod("dynamicportals")
public class DynamicPortals {
	// lorenzo: who needs LogManager.getLogger() when you have System.out :sunglasses:
	//private static final Logger LOGGER = LogManager.getLogger();
	
	public DynamicPortals() {
		DynamicPortalsNetworkRegistry.init();
		
		MinecraftForge.EVENT_BUS.register(DynamicPortalsConfig.class);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DynamicPortalsConfig.clientSpec);
		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.addListener(Renderer::onRenderEvent);
		}
		MinecraftForge.EVENT_BUS.addListener(DynamicPortals::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(DynamicPortals::onTick);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	public static void onTick(TickEvent.WorldTickEvent event) {
		if (event.side.isServer()) {
			Level level = event.world;
			for (AbstractPortal portal : Temp.getPortals(event.world)) {
				if (portal instanceof BasicPortal bap) {
					if (bap.tracker.isDirty()) {
						LevelChunk chunk = level.getChunkAt(new BlockPos(bap.raytraceOffset()));
						DynamicPortalsNetworkRegistry.NETWORK_INSTANCE.send(
								PacketDistributor.TRACKING_CHUNK.with(() -> chunk),
								new PortalUpdatePacket(bap)
						);
					}
				}
			}
		}
	}
	
	public static void registerCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(DynamicPortalsCommand.build(event.getDispatcher()));
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		if (ModList.get().isLoaded("pehkui")) PehkuiSupport.setup();
	}
}
