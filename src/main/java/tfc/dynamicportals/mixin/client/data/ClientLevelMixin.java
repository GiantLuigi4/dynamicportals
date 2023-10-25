package tfc.dynamicportals.mixin.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.level.LevelLoader;

import java.util.ArrayList;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements NetworkHolder {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Override
    public ArrayList<PortalNet> getPortalNetworks() {
        return ((NetworkHolder) minecraft).getPortalNetworks();
    }

    @Override
    public LevelLoader getLoader() {
        return ((NetworkHolder) minecraft).getLoader();
    }
}
