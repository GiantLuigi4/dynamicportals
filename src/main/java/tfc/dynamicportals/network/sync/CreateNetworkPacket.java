package tfc.dynamicportals.network.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tfc.dynamicportals.network.Packet;

public class CreateNetworkPacket extends Packet {
    public CreateNetworkPacket() {
    }

    public CreateNetworkPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        super.handle(ctx);
    }
}
