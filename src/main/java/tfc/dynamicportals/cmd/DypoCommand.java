package tfc.dynamicportals.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;

public class DypoCommand<T> implements Command<T> {
    @Override
    public int run(CommandContext<T> context) throws CommandSyntaxException {
        if (context.getSource() instanceof CommandSourceStack stk) {
            stk.sendSuccess(
                    new TextComponent("test"),
                    true
            );
        }
        return 0;
    }
}
