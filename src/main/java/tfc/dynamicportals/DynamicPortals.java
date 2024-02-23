package tfc.dynamicportals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.network.DypoNetworkRegistry;
import tfc.dynamicportals.network.sync.CreateNetworkPacket;
import tfc.dynamicportals.network.util.PortalPacketSender;
import tfc.dynamicportals.util.DypoShaders;

import javax.annotation.Nullable;
import java.io.IOException;

@Mod("dynamicportals")
public class DynamicPortals {
	// lorenzo: who needs LogManager.getLogger() when you have System.out :sunglasses:
	//private static final Logger LOGGER = LogManager.getLogger();
	
	public DynamicPortals(IEventBus bus) {
		DypoNetworkRegistry.init(bus);
		NeoForge.EVENT_BUS.addListener(DynamicPortals::onPlayerJoined);
		DypoShaders.init(bus);
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
					PacketDistributor.PLAYER.with((ServerPlayer) plyr).send(pkt[0]);
				});
				portalNetwork.sendPacket(sender);
			}
		}
	}
}
