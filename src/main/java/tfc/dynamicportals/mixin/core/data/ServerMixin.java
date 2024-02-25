package tfc.dynamicportals.mixin.core.data;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.level.LevelLoader;
import tfc.dynamicportals.level.ServerLevelLoader;

import java.util.ArrayList;

@Mixin(MinecraftServer.class)
public class ServerMixin implements NetworkHolder {
	
	ArrayList<PortalNet> nets = new ArrayList<>();

	ServerLevelLoader loader = new ServerLevelLoader((MinecraftServer) ((Object) this));

	@Override
	public ArrayList<PortalNet> getPortalNetworks() {
		return nets;
	}

	@Override
	public LevelLoader getLoader() {
		return loader;
	}
}
