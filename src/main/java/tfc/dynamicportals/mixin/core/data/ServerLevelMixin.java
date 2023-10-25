package tfc.dynamicportals.mixin.core.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements NetworkHolder {
	@Shadow
	@Nonnull
	public abstract MinecraftServer getServer();
	
	@Override
	public ArrayList<PortalNet> getPortalNetworks() {
		return ((NetworkHolder) getServer()).getPortalNetworks();
	}
}
