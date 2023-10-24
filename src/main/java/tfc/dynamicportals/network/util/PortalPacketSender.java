package tfc.dynamicportals.network.util;

import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.function.Consumer;

public class PortalPacketSender {
    HashSet<Player> sentTo = new HashSet<>();

    Consumer<Player> sender;

    public PortalPacketSender(Consumer<Player> sender) {
        this.sender = sender;
    }

    public void send(Player player) {
        if (sentTo.add(player)) {
            sender.accept(player);
        }
    }
}
