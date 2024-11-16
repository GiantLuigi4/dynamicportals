package tfc.dynamicportals.cmd;

import com.mojang.brigadier.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.resources.language.I18n;
import tfc.dynamicportals.cmd.nodes.CommandNodeAccessor;
import tfc.dynamicportals.cmd.nodes.DypoNode;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class DypoCmdNode<T> extends LiteralCommandNode<T> {
    String name;
    DypoNode<T> node;
    CommandDispatcher<T> dispatcher;

    public DypoCmdNode(
            CommandDispatcher<T> dispatcher,
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

//        dispatcher.getSmartUsage(this, null).put(
//                this, I18n.get("dynamicportals.command.bread.help")
//        );
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
        return "dynamic_portals";
    }

    @Override
    public void parse(StringReader reader, CommandContextBuilder<T> contextBuilder) throws CommandSyntaxException {
        DypoContextBuilder contextBuilder1 = new DypoContextBuilder(node);

        CommandSyntaxException err;
        if ((err = node.parse(reader, contextBuilder, contextBuilder1)) == null) {
            err = node.parseChildren(reader, contextBuilder, contextBuilder1);
        } else {
            CommandNodeAccessor.rethrow(err);
            throw new RuntimeException("wth");
        }
        contextBuilder1.finish(reader.getCursor());

        DataHolderNode<T> holderNode = new DataHolderNode<>(
                "__dypo_holder_node__",
                (c) -> 0,
                (c) -> true,
                null, null,
                false, this
        );
        holderNode.dctx = contextBuilder1;
        holderNode.len = reader.getCursor();
        contextBuilder.withNode(
                holderNode,
                contextBuilder.getRange()
        );

        if (err != null) {
            CommandNodeAccessor.rethrow(err);
        }
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext<T> context, SuggestionsBuilder builder) {
        try {
            if (context.getNodes().isEmpty()) {
                CommandContextBuilder<T> builder1 = new CommandContextBuilder<>(
                        null, context.getSource(),
                        context.getRootNode(), 1
                );
                try {
                    StringReader reader = new StringReader(context.getInput());
                    reader.read();
                    parse(
                            reader,
                            builder1
                    );
                    reader.skipWhitespace();
                } catch (Throwable err) {
                }
                context = builder1.build(context.getInput());
            }

            int cursor = 0;

            DypoContextBuilder ctx = null;
            for (ParsedCommandNode<T> contextNode : context.getNodes()) {
                if (
                        contextNode.getNode().getName().startsWith("__dypo_holder_node__") &&
                                contextNode.getNode() instanceof DataHolderNode<T> dhn
                ) {
                    ctx = dhn.dctx;
                    cursor = dhn.len;
                }
            }

            if (context.getInput().charAt(cursor) == ' ') {
                cursor += 1;
            }

            if (ctx == null || cursor <= getName().length() + 1) {
                ctx = new DypoContextBuilder(node);
                cursor = 1;
                ctx.suggestionOffset = cursor;
                builder = builder.createOffset(cursor);
                return ctx.lastNode.mySuggestions(context, builder, ctx);
            }

            ctx.suggestionOffset = cursor;
            return ctx.lastNode.fillSuggestions(context, builder, ctx);
        } catch (Throwable err) {
            if (err instanceof CommandSyntaxException ex)
                CommandNodeAccessor.rethrow(ex);
            else throw new RuntimeException(err);
            throw new RuntimeException("wth");
        }
    }

    @Override
    public LiteralArgumentBuilder<T> createBuilder() {
        // dummy builder that redirects to this object's methods
        return new DypoBuilder<>(this);
    }

    @Override
    protected String getSortedKey() {
        return getName();
    }

    @Override
    public Collection<String> getExamples() {
//        return node.getExamples();
        return Collections.singleton(getName());
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
        return false;
    }
}
