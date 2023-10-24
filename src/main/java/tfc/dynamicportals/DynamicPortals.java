package tfc.dynamicportals;

import net.minecraftforge.fml.common.Mod;
import tfc.dynamicportals.network.DypoNetworkRegistry;

@Mod("dynamicportals")
public class DynamicPortals {
	// lorenzo: who needs LogManager.getLogger() when you have System.out :sunglasses:
	//private static final Logger LOGGER = LogManager.getLogger();

	public DynamicPortals() {
		DypoNetworkRegistry.init();
	}
}
