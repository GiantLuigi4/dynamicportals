package tfc.dynamicportals.cmd.nodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public abstract class DypoNode<T> {
    public abstract void parse(StringReader reader, CommandContextBuilder<T> contextBuilder) throws CommandSyntaxException;

    public abstract boolean isValidInput(String string);

    public boolean isValidInput(StringReader reader) {
        return isValidInput(reader.getString().substring(reader.getCursor()));
    }

    public abstract Collection<String> getExamples();

    public abstract CompletableFuture<Suggestions> listSuggestions(final CommandContext<T> context, final SuggestionsBuilder builder) throws CommandSyntaxException;
}
