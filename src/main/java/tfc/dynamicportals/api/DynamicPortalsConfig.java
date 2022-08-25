package tfc.dynamicportals.api;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class DynamicPortalsConfig {
	
	public static final ForgeConfigSpec clientSpec;
	public static final DynamicPortalsConfig.Client CLIENT;
	
	@SubscribeEvent
	public static void onLoad(ModConfigEvent.Loading configEvent) {
		System.out.printf("Loaded Dynamic Portals config file %s%n", configEvent.getConfig().getFileName());
	}
	
	@SubscribeEvent
	public static void onFileChange(ModConfigEvent.Reloading configEvent) {
		System.out.println("Dynamic Portals config just got changed!");
	}
	
	public static class Client {
		public final ForgeConfigSpec.BooleanValue netherAnimation;
		
		Client(ForgeConfigSpec.Builder builder) {
			builder.comment("Client only settings").push("client");
			this.netherAnimation = builder.translation("dynamicportals.config.client.netherAnimation").define("netherAnimation", true);
			builder.pop();
		}
	}
	
	static {
		ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
		CLIENT = new DynamicPortalsConfig.Client(client);
		clientSpec = client.build();
	}
}
