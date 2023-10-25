package tfc.dynamicportals.mixin.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.ArrayList;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements NetworkHolder {
	@Override
	public ArrayList<PortalNet> getPortalNetworks() {
		return ((NetworkHolder) Minecraft.getInstance()).getPortalNetworks();
	}
}
