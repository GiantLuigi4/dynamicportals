package tfc.dynamicportals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.api.registry.PortalTypes;
import tfc.dynamicportals.client.renderer.BasicPortalRenderer;
import tfc.dynamicportals.client.renderer.NetherPortalRenderer;
import tfc.dynamicportals.cmd.DypoCommandRegistry;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.network.DypoNetworkRegistry;
import tfc.dynamicportals.network.sync.CreateNetworkPacket;
import tfc.dynamicportals.network.util.PortalPacketSender;
import tfc.dynamicportals.client.util.render.DypoShaders;

@Mod("dynamicportals")
public class DynamicPortals {
	// lorenzo: who needs LogManager.getLogger() when you have System.out :sunglasses:
	//private static final Logger LOGGER = LogManager.getLogger();
	
	public DynamicPortals() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		
		DypoNetworkRegistry.init(bus);

		MinecraftForge.EVENT_BUS.addListener(DynamicPortals::onPlayerJoined);
		MinecraftForge.EVENT_BUS.addListener(DypoCommandRegistry::registerC);
		DypoShaders.init(bus);
		
		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.addListener(DypoCommandRegistry::registerS);
			new BasicPortalRenderer(PortalTypes.BASIC);
			new NetherPortalRenderer(PortalTypes.NETHER);
		}
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
