package tfc.dynamicportals;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.dynamicportals.api.BasicPortal;
import tfc.dynamicportals.api.PortalRegistry;
import tfc.dynamicportals.api.PortalType;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("dynamicportals")
public class DynamicPortals {
	
	public static final PortalType BASIC_PORTAL = PortalRegistry.register(new ResourceLocation("dynamicportals:basic_portal"), (nbt) -> BasicPortal.load(nbt));
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	public DynamicPortals() {
//		FMLJavaModLoadingContext.get().getModEventBus().addListener();
//		MinecraftForge.EVENT_BUS.addListener();
		
		MinecraftForge.EVENT_BUS.addListener(Renderer::onRenderEvent);
		MinecraftForge.EVENT_BUS.addListener(Renderer::onBeginFrame);
	}
}
