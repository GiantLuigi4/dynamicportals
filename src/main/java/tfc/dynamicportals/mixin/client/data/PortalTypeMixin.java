package tfc.dynamicportals.mixin.client.data;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.client.api.AbstractPortalRenderer;
import tfc.dynamicportals.itf.RendererHolder;

@Mixin(PortalType.class)
public class PortalTypeMixin implements RendererHolder {
    @Unique
    AbstractPortalRenderer renderer;

    @Override
    public AbstractPortalRenderer getRenderer() {
        return renderer;
    }

    @Override
    public void setRenderer(AbstractPortalRenderer renderer) {
        if (this.renderer != null) throw new RuntimeException("Trying to reassign portal renderer.");
        this.renderer = renderer;
    }
}
