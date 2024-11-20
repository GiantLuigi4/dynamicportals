package tfc.dynamicportals.cmd.cmdr;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.util.Crypt;
import tfc.dynamicportals.cmd.cmdr.nodes.CmdRNode;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class CmdRBridgeNode<T, A, B> extends LiteralCommandNode<T> {
    String name;
    CmdRNode<T, A, B> node;
    CommandDispatcher<T> dispatcher;
    boolean isClient;

    protected static <T> Command<T> execution(boolean client) {
        if (client) {
            return (ctx) -> {
//                Instant instant = Instant.now();
//                long i = Crypt.SaltSupplier.getLong();
//                LastSeenMessagesTracker.Update lastseenmessagestracker$update = this.lastSeenMessages.generateAndApplyUpdate();
//                ArgumentSignatures argumentsignatures = ArgumentSignatures.signCommand(SignableCommand.of(this.parseCommand(pCommand)), (p_247875_) -> {
//                    SignedMessageBody signedmessagebody = new SignedMessageBody(p_247875_, instant, i, lastseenmessagestracker$update.lastSeen());
//                    return this.signedMessageEncoder.pack(signedmessagebody);
//                });
//                this.m_104955_(new ServerboundChatCommandPacket(pCommand, instant, i, argumentsignatures, lastseenmessagestracker$update.update()));
//
//                Minecraft.getInstance().player.connection.sendCommand(ctx.getInput());
//                return 0;
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
            };
        }

        return (context) -> {
            DataHolderNode<T> dhn = CommandNodeAccessor.getCmdRCtxNode(context);
            Object data = null;
            BiFunction<Object, Object, Integer> postAction = null;
            CmdRContext dctx = dhn.dctx;
            Stack<Object> dats = new Stack<>();
            for (CmdRNode dypoNode : dhn.dctx.nodes) {
                data = dypoNode.execute(context, data);
                if (dypoNode.getPostAction() != null) postAction = dypoNode.getPostAction();
                dats.push(dctx.getExecData());
                dctx.progress();
            }
            while (!dats.isEmpty()) {
                dctx.dataPoints.push(dats.pop());
            }
            if (postAction != null) return postAction.apply(context, data);
            return 0;
        };
    }

    public CmdRBridgeNode(
            CommandDispatcher<T> dispatcher,
            boolean isClient,
            String name,
            CmdRNode<T, A, B> node,
            Predicate<T> requirement,
            CommandNode<T> redirect,
            RedirectModifier<T> modifier,
            boolean forks
    ) {
        super(name, execution(isClient), requirement, redirect, modifier, forks);
        this.node = node;
        this.name = name;
        this.isClient = isClient;

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
        CmdRContext contextBuilder1 = new CmdRContext(node);

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

            CmdRContext ctx = null;
            for (ParsedCommandNode<T> contextNode : context.getNodes()) {
                if (
                        contextNode.getNode().getName().startsWith("__dypo_holder_node__") &&
                                contextNode.getNode() instanceof DataHolderNode<T> dhn
                ) {
                    ctx = dhn.dctx;
                    cursor = dhn.len;
                }
            }

            try {
                if (context.getInput().charAt(cursor) == ' ') {
                    cursor += 1;
                }
            } catch (Throwable err) {
                return CompletableFuture.completedFuture(builder.build());
            }

            if (ctx == null || cursor <= getName().length() + 1) {
                ctx = new CmdRContext(node);
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
        return new CmdRBuilder<>(this);
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
