package tfc.dynamicportals.mixin.core.data;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.ArrayList;

@Mixin(MinecraftServer.class)
public class ServerMixin implements NetworkHolder {
	ArrayList<PortalNet> nets = new ArrayList<>();
	
	@Override
	public ArrayList<PortalNet> getPortalNetworks() {
		return nets;
	}
}
