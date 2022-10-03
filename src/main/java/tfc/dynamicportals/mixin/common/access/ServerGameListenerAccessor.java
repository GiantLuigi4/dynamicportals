package tfc.dynamicportals.mixin.common.access;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.access.GameListenerAccessor;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGameListenerAccessor implements GameListenerAccessor {
	@Shadow public ServerPlayer player;
	
	@Override
	public Player getPlayer() {
		return player;
	}
}
