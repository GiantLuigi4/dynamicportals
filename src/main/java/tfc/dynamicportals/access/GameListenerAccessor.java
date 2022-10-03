package tfc.dynamicportals.access;

import net.minecraft.world.entity.player.Player;

public interface GameListenerAccessor {
	Player getPlayer();
	void setPosition(double x, double y, double z);
}
