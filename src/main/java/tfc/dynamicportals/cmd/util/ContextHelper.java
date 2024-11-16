package tfc.dynamicportals.cmd.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ContextHelper {
    public static <T> CompoundTag getLevelTag(CommandContext<T> ctx) {
        CompoundTag level = new CompoundTag();
        T t = ctx.getSource();
        if (t instanceof CommandSourceStack stack) {
            Level moj = stack.getUnsidedLevel();
            ResourceKey<Level> key = moj.dimension();
            level.putString("registry", key.registry().toString());
            level.putString("location", key.location().toString());
            return level;
        } else throw new RuntimeException("NYI: " + t.getClass());
    }
}
