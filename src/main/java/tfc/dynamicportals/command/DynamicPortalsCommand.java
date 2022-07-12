package tfc.dynamicportals.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.BasicPortal;
import tfc.dynamicportals.command.args.PortalSelectorArgument;
import tfc.dynamicportals.command.args.StringArrayArgument;
import tfc.dynamicportals.command.portals.BasicCommandPortal;
import tfc.dynamicportals.command.portals.BasicEndPortal;
import tfc.dynamicportals.command.portals.BasicNetherPortal;
import tfc.dynamicportals.util.DynamicPortalsSourceStack;
import tfc.dynamicportals.util.VecMath;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class DynamicPortalsCommand {
	private static final Map<String, Function<UUID, BasicPortal>> portalCreators = new HashMap<>();
	
	static {
		portalCreators.put("basic", BasicCommandPortal::new);
		portalCreators.put("nether", BasicNetherPortal::new);
		portalCreators.put("end", BasicEndPortal::new);
	}
	
	//NOW it's at its best
	public static LiteralArgumentBuilder<CommandSourceStack> build(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = literal("dynamicportals");
		builder.executes(context -> {
			context.getSource().sendSuccess(new TranslatableComponent("dynamicportals.command.bread.help"), true);
			return 0;
		});
		CommandNode<CommandSourceStack> commandNode = dispatcher.register(builder);
		buildSelfExecutingSubcommand("create", null, commandNode, context -> {
			DynamicPortalsSourceStack ctx;
			if (context.getSource() instanceof DynamicPortalsSourceStack)
				ctx = (DynamicPortalsSourceStack) context.getSource();
			else {
				context.getSource().sendFailure(new TranslatableComponent("dynamicportals.command.cheese.missing_args"));
				return -1;
			}
			Vec3 sizeVec = ctx.getPositionFromWorldCoordinates("size");
			if (sizeVec == null) {
				context.getSource().sendFailure(new TranslatableComponent("dynamicportals.command.cheese.missing_size"));
				return -1;
			}
			Vec3 normal = ctx.getPositionFromWorldCoordinates("normal");
			FullPortalFilter targetFilter = ctx.getArgument("target", FullPortalFilter.class);
			//noinspection SuspiciousNameCombination <- INTELLIJ
			BasicPortal newPortal = portalCreators
					.get(ctx.getArgumentOrDefault("type", String.class, "basic"))
					.apply(ctx.getArgumentOrDefault("uuid", UUID.class, new UUID(System.nanoTime(), context.getSource().getLevel().getGameTime())))
					.setPosition(ctx.getPositionFromWorldCoordinatesOrDefault("position", context.getSource().getPosition()))
					.setSize(sizeVec.x, sizeVec.z)
					.setRotation(VecMath.toRadians(ctx.getPositionFromWorldCoordinatesOrDefault("rotation", new Vec3(context.getSource().getRotation().y, -context.getSource().getRotation().x, 0))));
			if (normal != null)
				newPortal.setNormal(normal);
			if (ctx.getArgumentOrDefault("frontonly", String.class, "false").equals("true"))
				newPortal.computeNormal();
			
			if (targetFilter != null) {
				CommandPortal[] possibleTargets = Temp.filter(targetFilter, context);
				if (possibleTargets.length > 0) {
					CommandPortal target = possibleTargets[0];
					((AbstractPortal) target).setTarget(newPortal);
					newPortal.setTarget((AbstractPortal) target);
				} else {
					ctx.sendFailure(new TranslatableComponent("dynamicportals.command.cheese.invalid_target"));
				}
			}
			
			int newId = Temp.addPortal(context.getSource().getLevel(), (CommandPortal) newPortal);
			if (newPortal.target == newPortal)
				ctx.sendSuccess(new TranslatableComponent("dynamicportals.command.bread.mirror", newId), true);
			else ctx.sendSuccess(new TranslatableComponent("dynamicportals.command.bread.target", newId), true);
			return 0;
		});
		
		buildSelfExecutingSubcommand("delete", PortalSelectorArgument.create(), commandNode, context -> {
			FullPortalFilter i = null;
			try {
				i = context.getArgument("delete", FullPortalFilter.class);
			} catch (Throwable ignored) {}
			if (i == null) {
				context.getSource().sendFailure(new TranslatableComponent("dynamicportals.command.cheese.empty_argument"));
				return -1;
			}
			
			int count = 0;
			for (CommandPortal commandPortal : Temp.filter(i, context)) {
				if (((AbstractPortal) commandPortal).target != commandPortal)
					((AbstractPortal) commandPortal).target.setTarget(((AbstractPortal) commandPortal).target);
				Temp.remove(commandPortal.myId());
				count += 1;
			}
			
			context.getSource().sendSuccess(new TranslatableComponent("dynamicportals.command.bread.delete", count), true);
			return count;
		});
		
		buildRedirectedSubcommand("position", Vec3Argument.vec3(false), commandNode, DynamicPortalsCommand::toSource);
		buildRedirectedSubcommand("rotation", Vec3Argument.vec3(false), commandNode, DynamicPortalsCommand::toSource);
		buildRedirectedSubcommand("size", Vec2Argument.vec2(false), commandNode, DynamicPortalsCommand::toSource);
		buildRedirectedSubcommand("normal", Vec3Argument.vec3(false), commandNode, DynamicPortalsCommand::toSource);
		buildRedirectedSubcommand("type", StringArrayArgument.of(new String[]{"basic", "nether", "end"}), commandNode, DynamicPortalsCommand::toSource);
		buildRedirectedSubcommand("frontonly", StringArrayArgument.of(new String[]{"true", "false"}), commandNode, DynamicPortalsCommand::toSource);
		buildRedirectedSubcommand("uuid", UuidArgument.uuid(), commandNode, DynamicPortalsCommand::toSource);
		buildRedirectedSubcommand("target", PortalSelectorArgument.create(), commandNode, DynamicPortalsCommand::toSource);
		return builder;
	}
	
	// jank hack to forward information to the redirects
	private static CommandSourceStack toSource(CommandContext<CommandSourceStack> context) {
		int perm = 0;
		while (context.getSource().hasPermission(perm)) perm++;
		return new DynamicPortalsSourceStack(
				context.getSource().source,
				context.getSource().getPosition(),
				context.getSource().getRotation(),
				context.getSource().getLevel(),
				perm - 1,
				context.getSource().getTextName(),
				context.getSource().getDisplayName(),
				context.getSource().getServer(),
				context.getSource().getEntity(),
				context.getSource().silent,
				context.getSource().consumer,
				context.getSource().anchor,
				context.getInput(),
				context.getNodes(),
				context
		);
	}
	
	// give me something to work with, and I will butcher it until it works in a way which is easy to work with
	//:GWchadThink: epic luigi
	private static <T> void buildRedirectedSubcommand(String name, ArgumentType<T> type, CommandNode<CommandSourceStack> command, Function<CommandContext<CommandSourceStack>, CommandSourceStack> infoSupplier) {
		ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> subCommand = LiteralArgumentBuilder.literal(name);
		ArgumentBuilder<CommandSourceStack, RequiredArgumentBuilder<CommandSourceStack, T>> argument = RequiredArgumentBuilder.argument(name, type);
		argument.redirect(command, infoSupplier::apply);
		subCommand.then(argument);
		command.addChild(subCommand.build());
	}
	
	private static <T> void buildSelfExecutingSubcommand(String name, ArgumentType<T> type, CommandNode<CommandSourceStack> command, Command<CommandSourceStack> cmd) {
		ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> subCommand = LiteralArgumentBuilder.literal(name);
		if (type != null) {
			ArgumentBuilder<CommandSourceStack, RequiredArgumentBuilder<CommandSourceStack, T>> argument = RequiredArgumentBuilder.argument(name, type);
			argument.executes(cmd);
			subCommand.then(argument);
		} else {
			subCommand.executes(cmd);
		}
		command.addChild(subCommand.build());
	}
}
