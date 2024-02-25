package tfc.dynamicportals.itf;

import tfc.dynamicportals.client.renderer.AbstractPortalRenderer;

public interface ClientPortalType {
	AbstractPortalRenderer getRenderer();
	void setRenderer(AbstractPortalRenderer portal);
}
