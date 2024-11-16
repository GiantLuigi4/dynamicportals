package tfc.dynamicportals.cmd.nodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class VanillaNode<T> extends DypoNode<T> {
    CommandNode<T> node;

    public VanillaNode(LiteralCommandNode<?> node) {
        this.node = (CommandNode<T>) node;
    }

    public static <T> VanillaNode<T> literal(String text) {
        return new VanillaNode<>(
                LiteralArgumentBuilder.literal(text).build()
        );
    }

    @Override
    public void parse(StringReader reader, CommandContextBuilder<T> contextBuilder) throws CommandSyntaxException {
        node.parse(reader, contextBuilder);
    }

    public boolean isValidInput(String string) {
        return CommandNodeAccessor.isValidInput(node, string);
    }

    @Override
    public Collection<String> getExamples() {
        return node.getExamples();
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext<T> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandNodeAccessor.listSuggestions(node, context, builder);
    }
}
