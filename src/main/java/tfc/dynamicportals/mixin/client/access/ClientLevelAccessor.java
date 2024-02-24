package tfc.dynamicportals.mixin.client.access;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.itf.ClientLevelAccess;

@Mixin(ClientLevel.class)
public class ClientLevelAccessor implements ClientLevelAccess {
	@Shadow @Final private LevelRenderer levelRenderer;
	
	@Override
	public LevelRenderer dynamic_portals$getLevelRenderer() {
		return levelRenderer;
	}
}
