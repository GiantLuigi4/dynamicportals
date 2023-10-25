package tfc.dynamicportals.mixin.core.debug;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.itf.NetworkHolder;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin {
    @Shadow
    @Nullable
    public abstract ServerLevel getLevel(ResourceKey<Level> pDimension);

    @Inject(at = @At("RETURN"), method = "createLevels")
    public void preRun(CallbackInfo ci) {
        Level lvl = getLevel(ResourceKey.create(
                Registry.DIMENSION_REGISTRY, new ResourceLocation("minecraft:overworld")
        ));
        Level nether = getLevel(ResourceKey.create(
                Registry.DIMENSION_REGISTRY, new ResourceLocation("minecraft:the_nether")
        ));

        PortalNet net = new PortalNet(new UUID(
                98423, 23912
        ));
        BasicPortal bap0 = new BasicPortal(lvl);
        bap0.setPosition(16, 64, 0);
        net.link(bap0);
        BasicPortal bap1 = new BasicPortal(nether);
        bap1.setPosition(-16, 64, 0);
        net.link(bap1);
        ((NetworkHolder) this).getPortalNetworks().add(net);
    }
}
