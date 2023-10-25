package tfc.dynamicportals.mixin.client.data;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.ArrayList;

@Mixin(Minecraft.class)
public class MinecraftMixin implements NetworkHolder {
	ArrayList<PortalNet> nets = new ArrayList<>();
	
	@Override
	public ArrayList<PortalNet> getPortalNetworks() {
		return nets;
	}
}
