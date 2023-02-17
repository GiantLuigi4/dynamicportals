package tfc.dynamicportals.portals.vanilla;

import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.portals.vanilla.render.NetherPortalRenderer;

import java.util.UUID;

public class NetherPortal extends BasicPortal {
	public NetherPortal(UUID uuid) {
		super(uuid, BasicPortalTypes.NETHER);
		if (FMLEnvironment.dist.isClient())
			this.renderer = new NetherPortalRenderer(this, uuid);
	}
}
