package tfc.dynamicportals.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class DynamicPortalsCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		LiteralArgumentBuilder<CommandSourceStack> builder = literal("dynamicportals");
		LiteralArgumentBuilder<CommandSourceStack> create = literal("create");
		create.then(builder("position", BlockPosArgument.blockPos()).fork(create.getRedirect(), create.getRedirectModifier()));
		create.then(builder("eee", BlockPosArgument.blockPos()).fork(create.getRedirect(), create.getRedirectModifier()));
		create.then(builder("wa", BlockPosArgument.blockPos()).fork(create.getRedirect(), create.getRedirectModifier()));
		
		return builder;
	}
	
	private static <T> RequiredArgumentBuilder builder(String name, ArgumentType<T> argument) {
		return RequiredArgumentBuilder.argument(name, argument);
	}
}
