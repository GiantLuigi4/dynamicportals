package tfc.dynamicportals.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import tfc.dynamicportals.network.sync.CreateNetworkPacket;
import tfc.dynamicportals.network.sync.SyncLevelsPacket;

import java.util.ArrayList;
import java.util.HashMap;

public class DypoNetworkRegistry {
	public static final String networkingVersion = "1.0.0";
	protected static String serverVersion = "";
	
	public static final SimpleChannel NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("dynamicportals", "main"),
			() -> networkingVersion,
			(s) -> compareVersionsClient(networkingVersion, s),
			(s) -> compareVersionsServer(networkingVersion, s)
	);
	
	static HashMap<Class<?>, Integer> idsByClass = new HashMap<>();
	
	protected static int packetId(Packet pkt) {
		return idsByClass.get(pkt.getClass());
	}
	
	//public static void register(RegisterPayloadHandlerEvent event) {
	public static void register() {
//		IPayloadRegistrar registrar =
//				event.registrar("dynamic_portals")
//						.versioned(networkingVersion);
		
		ArrayList<NetworkEntry<?>> entries = new ArrayList<>();
		entries.add(new NetworkEntry<>(CreateNetworkPacket.class, CreateNetworkPacket::new));
		entries.add(new NetworkEntry<>(SyncLevelsPacket.class, SyncLevelsPacket::new));
		
		HashMap<Class<?>, NetworkEntry<?>> entriesByClass = new HashMap<>();
		for (int i = 0; i < entries.size(); i++) {
			NetworkEntry entry = entries.get(i);
			entriesByClass.put(entry.clazz, entry);
			idsByClass.put(entry.clazz, i);
		}

//		registrar.play(
//				new ResourceLocation("dynamic_portals:uber"),
//				(pkt) -> {
//					short s = pkt.readShort();
//					NetworkEntry<?> entry = entries.get(s);
//					return entry.fabricator.apply(pkt);
//				},
//				Packet::handle
//		);
		int idx = 0;
		for (NetworkEntry<?> entry : entries) {
			NETWORK_INSTANCE
					.messageBuilder((Class<Packet>) entry.clazz, idx++)
					.encoder(Packet::writeData)
					.decoder((d) -> entry.fabricator.apply(d))
					.consumerNetworkThread((pkt, ctx) -> {
						pkt.handle(ctx.get());
					})
					.add();
		}
	}
	
	public static void init(IEventBus bus) {
//		bus.addListener(DypoNetworkRegistry::register);
		register();
	}
	
	public static boolean compareVersionsServer(String str0, String str1) {
		if (str1.contains("compat")) return true;
		str0 = str0.split("compat")[0];
		str1 = str0.split("compat")[0];
		String[] serverVer = parseVersion(str0);
		String[] clientVer = parseVersion(str1);
		serverVer = addPlaceholders(serverVer, clientVer);
		clientVer = addPlaceholders(clientVer, serverVer);
		serverVersion = str0;
		
		if (serverVer.length == 0 || clientVer.length == 0) return false;
		if (!serverVer[0].equals(clientVer[0])) return false;
		
		if (serverVer.length < 2 || clientVer.length < 2) return false;
		// server uses newer server sub than client
		// client is allowed
		if (Integer.parseInt(clientVer[1]) >= Integer.parseInt(serverVer[1])) return true;
		if (serverVer.length > 2 && clientVer.length > 2) {
			// server uses older client version than client
			// client is allowed
			if (Integer.parseInt(clientVer[2]) <= Integer.parseInt(serverVer[2])) return false;
		} else {
			// client does not have sub but server does
			// client uses older client networking version
			// client is allowed
			if (serverVer.length > clientVer.length) return false;
		}
		return false;
	}
	
	public static boolean compareVersionsClient(String str0, String str1) {
		if (str0.contains("compat")) return true;
		str0 = str0.split("compat")[0];
		str1 = str0.split("compat")[0];
		String[] clientVer = parseVersion(str0);
		String[] serverVer = parseVersion(str1);
		clientVer = addPlaceholders(clientVer, serverVer);
		serverVer = addPlaceholders(serverVer, clientVer);
		serverVersion = str1;
		
		if (clientVer.length == 0 || serverVer.length == 0) return false;
		if (!clientVer[0].equals(serverVer[0])) return false;
		
		if (clientVer.length < 2 || serverVer.length < 2) return false;
		if (Integer.parseInt(serverVer[1]) <= Integer.parseInt(clientVer[1])) return true;
		if (clientVer.length > 2 && serverVer.length > 2) {
			if (Integer.parseInt(serverVer[2]) >= Integer.parseInt(clientVer[2])) return false;
		} else {
			if (clientVer.length > serverVer.length) return false;
		}
		return false;
	}
	
	public static String[] parseVersion(String input) {
		if (input.contains(".")) {
			return input.split("\\.");
		}
		return new String[]{input};
	}
	
	public static String[] addPlaceholders(String[] ver0, String[] ver1) {
		int len = Math.max(ver0.length, ver1.length);
		String[] strs = new String[len];
		for (int i = 0; i < len; i++) {
			if (i < ver0.length) {
				strs[i] = ver0[i];
			} else {
				strs[i] = "0";
			}
		}
		return strs;
	}
	
	public static void sendToServer(Packet packet) {
		NETWORK_INSTANCE.sendToServer(packet);
	}
	
	public static void send(Packet packet, PacketDistributor.PacketTarget target) {
		NETWORK_INSTANCE.send(target, packet);
	}
}
