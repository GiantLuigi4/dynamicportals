package tfc.dynamicportals.command.registry;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.api.registry.PortalTypes;
import tfc.dynamicportals.command.DynamicPortalsCommand;
import tfc.dynamicportals.command.arg.OrientationArgument;

import java.util.HashMap;
import java.util.function.Consumer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PortalTypeCommands {
	static HashMap<String, Consumer<ArgumentBuilder<CommandSourceStack, ?>>> consumers = new HashMap<>();
	
	public static void fillDefault(PortalType<?> type, ArgumentBuilder<CommandSourceStack, ?> builder) {
		builder
				.then(argument("position", Vec3Argument.vec3())
						.then(argument("size", Vec2Argument.vec2())
								.then(argument("rotation", OrientationArgument.vec3())
										.executes((ctx) -> DynamicPortalsCommand.createStandardPortal(type, ctx)))));
	}
	
	static {
		add(PortalTypes.BASIC, (builder) -> {
			fillDefault(PortalTypes.BASIC, builder);
		});
	}
	
	public static void add(
			PortalType<?> typeFor,
			Consumer<ArgumentBuilder<CommandSourceStack, ?>> branch
	) {
		consumers.put(typeFor.getRegistryName().toString(), branch);
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> fill(LiteralArgumentBuilder<CommandSourceStack> typeRoot) {
		consumers.forEach((k, v) -> {
			ArgumentBuilder<CommandSourceStack, ?> builder = literal(k);
			v.accept(builder);
			typeRoot.then(builder);
		});
		return typeRoot;
	}
}
