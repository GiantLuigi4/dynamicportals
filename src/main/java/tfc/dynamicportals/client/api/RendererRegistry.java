package tfc.dynamicportals.client.api;

import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.client.api.impl.BasicPortalRenderer;
import tfc.dynamicportals.itf.RendererHolder;

public class RendererRegistry {
    static {
        registerRenderer(
                BasicPortalTypes.BASIC,
                new BasicPortalRenderer()
        );
    }

    public static <T extends BasicPortal> void registerRenderer(
            PortalType<T> type,
            AbstractPortalRenderer<T> renderer
    ) {
        ((RendererHolder) type).setRenderer(renderer);
    }

    public static void init() {
    }
}
