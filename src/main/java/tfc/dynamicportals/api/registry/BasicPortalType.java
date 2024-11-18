package tfc.dynamicportals.api.registry;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.cmd.DypoCommandRegistry;
import tfc.dynamicportals.cmd.cmdr.nodes.CmdRNode;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.function.BiFunction;

public class BasicPortalType<T extends AbstractPortal> extends PortalType<T>{
    public BasicPortalType(BiFunction<NetworkHolder, CompoundTag, T> fromNbt) {
        super(fromNbt);
    }

    @Override
    public boolean supportsCommand() {
        return true;
    }

    @Override
    public <T extends CommandContext<V>, V> void fillCommand(CmdRNode<T, CompoundTag, CompoundTag> create, CmdRNode<T, CompoundTag, CompoundTag> modify) {
        DypoCommandRegistry.fillBasic(this, create, modify);
    }
}
