package tfc.dynamicportals.itf;

import tfc.dynamicportals.client.api.AbstractPortalRenderer;

public interface RendererHolder {
    AbstractPortalRenderer getRenderer();
    void setRenderer(AbstractPortalRenderer renderer);
}
