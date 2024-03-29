package tfc.dynamicportals.mixin.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.level.ClientLevelLoader;
import tfc.dynamicportals.level.LevelLoader;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Mixin(Minecraft.class)
public class MinecraftMixin implements NetworkHolder {
    @Shadow @Nullable public ClientLevel level;
    ArrayList<PortalNet> nets = new ArrayList<>();

    ClientLevelLoader loader = new ClientLevelLoader((Minecraft) (Object) this);

    @Override
    public ArrayList<PortalNet> getPortalNetworks() {
        return nets;
    }

    @Override
    public LevelLoader getLoader() {
        return loader;
    }

    @Inject(at = @At("TAIL"), method = "setLevel")
    public void updateNets(ClientLevel pLevelClient, CallbackInfo ci) {
        for (PortalNet net : nets) {
            loader.update(level);
            net.correct(this);
        }
    }

    @Inject(at = @At("HEAD"), method = "disconnect")
    public void preClear(CallbackInfo ci) {
		nets.clear();
        loader.dump();
    }
}
