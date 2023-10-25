package tfc.dynamicportals.mixin.core.data;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.level.LevelLoader;

import java.util.ArrayList;

@Mixin(Level.class)
public class LevelMixin implements NetworkHolder {
	@Override
	public ArrayList<PortalNet> getPortalNetworks() {
		return null;
	}

	@Override
	public LevelLoader getLoader() {
		return null;
	}
}
