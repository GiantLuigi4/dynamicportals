package tfc.dynamicportals.mixin.client.data;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.level.ClientLevelLoader;
import tfc.dynamicportals.level.LevelLoader;

import java.util.ArrayList;

@Mixin(Minecraft.class)
public class MinecraftMixin implements NetworkHolder {
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
}
