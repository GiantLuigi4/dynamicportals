package tfc.dynamicportals.cmd;

import com.mojang.brigadier.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSourceStack;
import tfc.dynamicportals.cmd.nodes.CommandNodeAccessor;
import tfc.dynamicportals.cmd.nodes.DypoNode;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class DypoCmdNode<T> extends LiteralCommandNode<T> {
    String name;
    DypoNode<T> node;

    public DypoCmdNode(
            String name,
            DypoNode<T> node,
            Command<T> command,
            Predicate<T> requirement,
            CommandNode<T> redirect,
            RedirectModifier<T> modifier,
            boolean forks
    ) {
//        super(command, requirement, redirect, modifier, forks);
        super(name, command, requirement, redirect, modifier, forks);
        this.node = node;
        this.name = name;
    }

    @Override
    public boolean isValidInput(String input) {
        return node.isValidInput(input);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUsageText() {
        return I18n.get("dynamicportals.command.bread.help");
    }

    @Override
    public void parse(StringReader reader, CommandContextBuilder<T> contextBuilder) throws CommandSyntaxException {
        // TODO: do this properly
        node.parse(reader, contextBuilder);
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext<T> context, SuggestionsBuilder builder) {
        try {
            return node.listSuggestions(context, builder);
        } catch (CommandSyntaxException err) {
            CommandNodeAccessor.rethrow(err);
            throw new RuntimeException("wth");
        }
    }

    @Override
    public LiteralArgumentBuilder<T> createBuilder() {
        // dummy builder so the game doesn't crash
        return new DypoBuilder<>(this);
    }

    @Override
    protected String getSortedKey() {
        return getName();
    }

    @Override
    public Collection<String> getExamples() {
        return node.getExamples();
    }

    @Override
    public void addChild(CommandNode<T> node) {
        throw new RuntimeException("Dypo uses a special command format; use DypoNode");
    }

    @Override
    public void findAmbiguities(AmbiguityConsumer<T> consumer) {
        // no-op
    }

    @Override
    public boolean isFork() {
        return true;
    }
}
