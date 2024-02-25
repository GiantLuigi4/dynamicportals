package tfc.dynamicportals;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.command.DynamicPortalsCommand;
import tfc.dynamicportals.command.arg.PortalSelectorArgument;
import tfc.dynamicportals.command.arg.OrientationArgument;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.network.DypoNetworkRegistry;
import tfc.dynamicportals.network.sync.CreateNetworkPacket;
import tfc.dynamicportals.network.util.PortalPacketSender;
import tfc.dynamicportals.util.DypoShaders;

@Mod("dynamicportals")
public class DynamicPortals {
	// lorenzo: who needs LogManager.getLogger() when you have System.out :sunglasses:
	//private static final Logger LOGGER = LogManager.getLogger();
	
	public DynamicPortals() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		
		DypoNetworkRegistry.init(bus);
		
		DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, "dynamicportals");
		COMMAND_ARGUMENT_TYPES.register("portal_selector", () -> ArgumentTypeInfos.registerByClass(PortalSelectorArgument.class, SingletonArgumentInfo.contextFree(PortalSelectorArgument::create)));
		COMMAND_ARGUMENT_TYPES.register("orientation", () -> ArgumentTypeInfos.registerByClass(OrientationArgument.class, SingletonArgumentInfo.contextFree(OrientationArgument::vec3)));
		COMMAND_ARGUMENT_TYPES.register(bus);
		
		MinecraftForge.EVENT_BUS.addListener(DynamicPortals::onPlayerJoined);
		MinecraftForge.EVENT_BUS.addListener(DynamicPortals::registerCommands);
		DypoShaders.init(bus);
	}
	
	public static void registerCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(DynamicPortalsCommand.build());
	}
	
	public static void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
		Level lvl = event.getEntity().level();
		
		if (lvl instanceof ServerLevel) {
			CreateNetworkPacket[] pkt = new CreateNetworkPacket[1];
			for (PortalNet portalNetwork : ((NetworkHolder) lvl).getPortalNetworks()) {
				pkt[0] = null;
				PortalPacketSender sender = new PortalPacketSender((plyr) -> {
					if (pkt[0] == null)
						pkt[0] = new CreateNetworkPacket(portalNetwork);
					DypoNetworkRegistry.send(pkt[0], PacketDistributor.PLAYER.with(() -> (ServerPlayer) plyr));
				});
				portalNetwork.sendPacket(sender);
			}
		}
	}
}
