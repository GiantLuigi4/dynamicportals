package tfc.dynamicportals.vanilla;

import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.vanilla.render.EndPortalRenderer;

import java.util.UUID;

public class EndPortal extends BasicPortal {
	public EndPortal(UUID uuid) {
		super(uuid);
		if (FMLEnvironment.dist.isClient())
			this.renderer = new EndPortalRenderer(this);
	}
}
