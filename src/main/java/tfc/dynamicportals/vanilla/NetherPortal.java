package tfc.dynamicportals.vanilla;

import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.vanilla.render.NetherPortalRenderer;

import java.util.UUID;

public class NetherPortal extends BasicPortal {
	public NetherPortal(UUID uuid) {
		super(uuid);
		if (FMLEnvironment.dist.isClient())
			this.renderer = new NetherPortalRenderer(this, uuid);
	}
}
