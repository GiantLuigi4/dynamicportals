package tfc.dynamicportals.portals.mirror;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.portals.mirror.renderer.MirrorRenderer;

import java.util.UUID;

public class Mirror extends BasicPortal {
	public Mirror(UUID uuid) {
		super(uuid, BasicPortalTypes.END);
		if (FMLEnvironment.dist.isClient())
			this.renderer = new MirrorRenderer(this);
	}
	
	@Override
	public boolean canTeleport(Entity entity, Vec3 position) {
		return super.canTeleport(entity, position);
	}
}
