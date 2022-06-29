package tfc.dynamicportals.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class DynamicPortalsCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> build(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = literal("dynamicportals");
		LiteralArgumentBuilder<CommandSourceStack> create = literal("create");
		Command<CommandSourceStack> cmd = context -> {
			return 0;
		};
		create.executes(cmd);
//		CommandNode<CommandSourceStack> commandNode = create.build();
		CommandNode<CommandSourceStack> commandNode = dispatcher.register(builder);
		builderFork("position", Vec3Argument.vec3(), commandNode, (context) -> {
			context.getSource().withPosition(context.getArgument("position", Vec3.class));
			return Collections.singleton(context.getSource());
		}, cmd);
		builderFork("rotation", Vec3Argument.vec3(), commandNode, DynamicPortalsCommand::toSource, cmd);
		builderFork("size", Vec2Argument.vec2(), commandNode, DynamicPortalsCommand::toSource, cmd);
		builderFork("normal", Vec3Argument.vec3(), commandNode, DynamicPortalsCommand::toSource, cmd);
		commandNode.addChild(create.build());
		
//		builder.then(commandNode);
		
		return builder;
	}
	
	private static Collection<CommandSourceStack> toSource(CommandContext<CommandSourceStack> context) {
		return Collections.singleton(context.getSource());
	}
	
	// give me something to work with, and I will butcher it until it works in a way which is easy to work with
	private static <T> void builderFork(String name, ArgumentType<T> type, CommandNode<CommandSourceStack> root, Function<CommandContext<CommandSourceStack>, Collection<CommandSourceStack>> infoSupplier, Command<CommandSourceStack> cmd) {
		ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> builder = LiteralArgumentBuilder.literal(name);
		ArgumentBuilder<CommandSourceStack, RequiredArgumentBuilder<CommandSourceStack, T>> builder1 = RequiredArgumentBuilder.argument(name, type);
		builder1.fork(root, infoSupplier::apply);
		builder.then(builder1);
		root.addChild(builder.build());
	}
}
