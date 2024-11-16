package tfc.dynamicportals.cmd;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import sun.misc.Unsafe;
import tfc.dynamicportals.cmd.exception.DypoException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class CommandNodeAccessor {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final MethodHandle isValidInput;
    private static final MethodHandle listSuggestions;

    private static final Unsafe theUnsafe;

    static {
        try {
            Method m = CommandNode.class.getDeclaredMethod("isValidInput", String.class);
            boolean isAccessible = m.isAccessible();
            m.setAccessible(true);
            isValidInput = lookup.unreflect(m);
            m.setAccessible(isAccessible);

            m = CommandNode.class.getDeclaredMethod("listSuggestions", CommandContext.class, SuggestionsBuilder.class);
            isAccessible = m.isAccessible();
            m.setAccessible(true);
            listSuggestions = lookup.unreflect(m);
            m.setAccessible(isAccessible);

            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            isAccessible = f.isAccessible();
            f.setAccessible(true);
            theUnsafe = (Unsafe) f.get(null);
            f.setAccessible(isAccessible);
        } catch (Throwable err) {
            throw new RuntimeException(err);
        }
    }

    public static <T> CompletableFuture<Suggestions> listSuggestions(
            CommandNode<T> node,
            CommandContext<T> context,
            SuggestionsBuilder builder
    ) throws CommandSyntaxException {
        try {
            return (CompletableFuture<Suggestions>) listSuggestions.invoke(node, context, builder);
        } catch (Throwable err) {
            theUnsafe.throwException(err);
            throw new RuntimeException("wth");
        }
    }

    public static <T> boolean isValidInput(
            CommandNode<T> node,
            String input
    ) {
        try {
            return (boolean) (Boolean) isValidInput.invoke(node, input);
        } catch (Throwable err) {
            theUnsafe.throwException(err);
            throw new RuntimeException("wth");
        }
    }

    public static void rethrow(CommandSyntaxException err) {
        theUnsafe.throwException(err);
    }

    public static <T> void setCtx(CommandContextBuilder<T> contextBuilder, CommandContextBuilder<T> builder) {
        contextBuilder.getNodes().addAll(builder.getNodes());
        contextBuilder.getArguments().putAll(builder.getArguments());
        contextBuilder.withCommand(builder.getCommand());
        contextBuilder.withSource(builder.getSource());
    }

    static <T> DataHolderNode getDpCtx(CommandContext<T> context) {
        for (int i = context.getNodes().size() - 1; i >= 0; i--) {
            ParsedCommandNode<T> node = context.getNodes().get(i);
            if (node.getNode().getName().startsWith("__dypo_holder_node__")) {
                return ((DataHolderNode) node.getNode());
            }
        }
        try {
            throw new DypoException("Missing dypo holder node");
        } catch (CommandSyntaxException err) {
            theUnsafe.throwException(err);
            throw new RuntimeException("wth");
        }
    }

    public static void throwUnchecked(CommandSyntaxException nyi) {
        try {
            throw nyi;
        } catch (CommandSyntaxException exception) {
            theUnsafe.throwException(exception);
        }
    }
}
