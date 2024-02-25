package tfc.dynamicportals.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.client.renderer.AbstractPortalRenderer;
import tfc.dynamicportals.itf.ClientPortalType;

// I love mixin
// I also hate mixin
@Mixin(PortalType.class)
class ClientPortalTypeAttachments implements ClientPortalType {
	AbstractPortalRenderer renderer;
	
	
	@Override
	public AbstractPortalRenderer getRenderer() {
		return renderer;
	}
	
	@Override
	public void setRenderer(AbstractPortalRenderer renderer) {
		if (this.renderer != null)
			throw new RuntimeException(((PortalType) (Object) this).getRegistryName().toString() + " already has a renderer");
		this.renderer = renderer;
	}
}
