package tfc.dynamicportals.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;

import java.util.concurrent.atomic.AtomicReference;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class DynamicPortalsCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> build(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = literal("dynamicportals");
		LiteralArgumentBuilder<CommandSourceStack> create = literal("create");
		CommandNode commandNode = create.build();
		// this is jank, but it works
		loopback(builderFork("position", Vec3Argument.vec3(), commandNode, commandNode.getRedirectModifier()), commandNode, commandNode);
		loopback(builderFork("rotation", Vec3Argument.vec3(), commandNode, commandNode.getRedirectModifier()), commandNode, commandNode);
		loopback(builderFork("size", Vec2Argument.vec2(), commandNode, commandNode.getRedirectModifier()), commandNode, commandNode);
		loopback(builderFork("normal", Vec3Argument.vec3(), commandNode, commandNode.getRedirectModifier()), commandNode, commandNode);
		builder.then(commandNode);
		
		return builder;
	}
	
	private static void loopback(ArgumentBuilder src, CommandNode dst, CommandNode root) {
		dst.addChild(src.build());
	}
	
	private static <T> RequiredArgumentBuilder genericsAreDumb(String name, ArgumentType<T> argument) {
		return RequiredArgumentBuilder.argument(name, argument);
	}
	
	private static ArgumentBuilder yeetGenerics(ArgumentBuilder builder) {
		return builder;
	}
	
	private static <T, V> ArgumentBuilder<CommandSourceStack, ?> builderFork(String name, ArgumentType<T> argument, CommandNode node, RedirectModifier<V> modif) {
		ArgumentBuilder builder = LiteralArgumentBuilder.literal(name);
		ArgumentBuilder builder1 = genericsAreDumb(name, argument);
		builder1 = builder1.forward(node, modif, false);
		builder.then(builder1);
		return builder;
	}
	
	private static <T> LiteralArgumentBuilder<CommandSourceStack> builder(String name, ArgumentType<T> argument) {
		return LiteralArgumentBuilder.literal(name).then(genericsAreDumb(name, argument));
	}
}
