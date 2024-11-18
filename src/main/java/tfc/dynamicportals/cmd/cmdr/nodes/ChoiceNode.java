package tfc.dynamicportals.cmd.cmdr.nodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tfc.dynamicportals.cmd.cmdr.CmdRContext;
import tfc.dynamicportals.cmd.cmdr.exception.DypoException;

import java.util.concurrent.CompletableFuture;

public class ChoiceNode<T, A, B> extends CmdRNode<T, A, B> {
    boolean denyRepeat = false;

    public ChoiceNode() {
    }

    public ChoiceNode(boolean denyRepeat) {
        this.denyRepeat = denyRepeat;
    }

    @Override
    public CommandSyntaxException parse(StringReader reader, CommandContextBuilder<T> builder, CmdRContext dpbuilder) {
        for (CmdRNode<T, B, ?> child : children) {
            int cursor = reader.getCursor();
            CommandSyntaxException ex = child.parse(reader, builder.copy(), new CmdRContext(child, dpbuilder));
            if (ex == null) {
                reader.setCursor(cursor);
                return null;
            }
            reader.setCursor(cursor);
        }
        return new DypoException(
                "Incorrect argument for command",
                reader
        );
    }

    @Override
    public boolean isValidInput(String input) {
        return true;
    }

    @Override
    public CompletableFuture<Suggestions> mySuggestions(CommandContext<T> context, SuggestionsBuilder builder, CmdRContext ctx) {
        return fillSuggestions(context, builder, ctx);
    }

    public ChoiceNode<T, A, B> requireArg(CmdRNode<T, B, B> arg) {
        addArg(arg);
        return this;
    }
}
