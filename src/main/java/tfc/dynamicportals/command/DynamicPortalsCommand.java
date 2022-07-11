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
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec2;
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
import tfc.dynamicportals.util.Vec2d;

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
	
	public static LiteralArgumentBuilder<CommandSourceStack> build(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = literal("dynamicportals");
		builder.executes(context -> {
			context.getSource().sendSuccess(new TranslatableComponent("dynamicportals.command.bread.help"), false);
			return 0;
		});
		
		LiteralArgumentBuilder<CommandSourceStack> create = literal("create");
		// the command execution function
		Command<CommandSourceStack> cmd = context -> {
			DynamicPortalsSourceStack ctx;
			if (context.getSource() instanceof DynamicPortalsSourceStack)
				ctx = (DynamicPortalsSourceStack) context.getSource();
			else {
				context.getSource().sendFailure(new TranslatableComponent("dynamicportals.command.cheese.missing_args"));
				return -1;
			}
			WorldCoordinates size = ctx.getArgument("size", WorldCoordinates.class);
			if (size == null) {
				context.getSource().sendFailure(new TranslatableComponent("dynamicportals.command.cheese.missing_size"));
				return -1;
			}
			WorldCoordinates pos = ctx.getArgument("position", WorldCoordinates.class);
			WorldCoordinates rot = ctx.getArgument("rotation", WorldCoordinates.class);
			WorldCoordinates norm = ctx.getArgument("normal", WorldCoordinates.class);
			UUID uuid = new UUID(System.nanoTime(), context.getSource().getLevel().getGameTime());
			try {
				UUID cmdUUid = ctx.getArgument("uuid", UUID.class);
				if (cmdUUid != null) uuid = cmdUUid;
			} catch (Throwable ignored) {
			}
			String type = "basic";
			try {
				String cmdType = ctx.getArgument("type", String.class);
				if (cmdType != null) type = cmdType;
			} catch (Throwable ignored) {
			}
			BasicPortal newPortal = portalCreators.get(type).apply(uuid);
			Vec3 vec;
			try {
				vec = pos.getPosition(ctx);
			} catch (Throwable ignored) {
				vec = context.getSource().getPosition();
			}
			Vec3 rotation;
			try {
				Vec3 ve = rot.getPosition(ctx);
				rotation = new Vec3(Math.toRadians(ve.x), Math.toRadians(ve.y), Math.toRadians(ve.z));
			} catch (Throwable ignored) {
				Vec2 rotato = context.getSource().getRotation();
				rotation = new Vec3(Math.toRadians(rotato.y), -Math.toRadians(rotato.x), 0);
			}
			Vec3 normal;
			try {
				normal = norm.getPosition(ctx);
			} catch (Throwable ignored) {
				normal = null;
			}
			newPortal.setPosition(vec.x, vec.y, vec.z);
			Vec2d sizeVec;
			try {
				Vec3 vec1 = size.getPosition(ctx);
				sizeVec = new Vec2d(vec1.x, vec1.z);
			} catch (Throwable ignored) {
				context.getSource().sendFailure(new TranslatableComponent("dynamicportals.command.cheese.size_crab"));
				return -1;
			}
			newPortal.setSize(sizeVec.x, sizeVec.y);
			newPortal.setRotation(rotation.x, rotation.y, rotation.z);
			if (normal != null) {
				newPortal.setNormal(normal);
			}
			try {
				if (ctx.getArgument("frontonly", String.class).equals("true"))
					newPortal.computeNormal();
			} catch (Throwable ignored) {
			}

			int newId = Temp.addPortal(context.getSource().getLevel(), (CommandPortal) newPortal);
			ctx.sendSuccess(new TranslatableComponent("dynamicportals.command.bread.id", newId), log(context));
			FullPortalFilter targetFilter = ctx.getArgument("target", FullPortalFilter.class);
			if (targetFilter != null) {
				CommandPortal[] possibleTargets = Temp.filter(targetFilter, context);
				if (possibleTargets.length > 0) {
					CommandPortal target = possibleTargets[0];
					((AbstractPortal) target).target = newPortal;
					newPortal.target = (AbstractPortal) target;
				} else {
					ctx.sendFailure(new TranslatableComponent("dynamicportals.command.cheese.invalid_target"));
				}
			}
			if (newPortal.target == newPortal)
				ctx.sendSuccess(new TranslatableComponent("dynamicportals.command.bread.mirror", newId), log(context));
			else ctx.sendSuccess(new TranslatableComponent("dynamicportals.command.bread.target", newId), log(context));
			return 0;
		};
		// TODO: provide help when command is executed with no arguments
		create.executes(cmd);
		CommandNode<CommandSourceStack> commandNode = dispatcher.register(builder);
		// arguments
		builderFork("position", Vec3Argument.vec3(false), commandNode, DynamicPortalsCommand::toSource, cmd);
		builderFork("rotation", Vec3Argument.vec3(false), commandNode, DynamicPortalsCommand::toSource, cmd);
		builderFork("size", Vec2Argument.vec2(false), commandNode, DynamicPortalsCommand::toSource, cmd);
		builderFork("normal", Vec3Argument.vec3(false), commandNode, DynamicPortalsCommand::toSource, cmd);
		builderFork("type", StringArrayArgument.of(new String[]{"basic", "nether", "end"}), commandNode, DynamicPortalsCommand::toSource, cmd);
		builderFork("frontonly", StringArrayArgument.of(new String[]{"true", "false"}), commandNode, DynamicPortalsCommand::toSource, cmd);
		builderFork("uuid", UuidArgument.uuid(), commandNode, DynamicPortalsCommand::toSource, cmd);
		builderFork("target", PortalSelectorArgument.create(), commandNode, DynamicPortalsCommand::toSource, cmd);
		commandNode.addChild(create.build());
		
		{
			ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> deleteBuilder = LiteralArgumentBuilder.literal("delete");
			ArgumentBuilder<CommandSourceStack, RequiredArgumentBuilder<CommandSourceStack, FullPortalFilter>> targetBuilder = RequiredArgumentBuilder.argument("target", PortalSelectorArgument.create());
			Command<CommandSourceStack> exec = context -> {
				FullPortalFilter i = context.getArgument("target", FullPortalFilter.class);
				int count = 0;
				for (CommandPortal commandPortal : Temp.filter(i, context)) {
					if (((AbstractPortal) commandPortal).target != commandPortal)
						((AbstractPortal) commandPortal).target.setTarget(((AbstractPortal) commandPortal).target);
					Temp.remove(commandPortal.myId());
					count += 1;
				}

				context.getSource().sendSuccess(new TranslatableComponent("dynamicportals.command.bread.delete", count), log(context));
				return count;
			};
			deleteBuilder.executes(exec);
			targetBuilder.executes(exec);
			deleteBuilder.then(targetBuilder);
			commandNode.addChild(deleteBuilder.build());
		}
		
		return builder;
	}
	
	//TODO: see if it can be removed
	private static boolean log(CommandContext<CommandSourceStack> context) {
		boolean log = true;
		if (context.getSource().hasPermission(4)) {
			if (context.getSource().getLevel().getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
				log = false;
			}
		}
		return log;
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
	private static <T> void builderFork(String name, ArgumentType<T> type, CommandNode<CommandSourceStack> root, Function<CommandContext<CommandSourceStack>, CommandSourceStack> infoSupplier, Command<CommandSourceStack> cmd) {
		ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> builder = LiteralArgumentBuilder.literal(name);
		ArgumentBuilder<CommandSourceStack, RequiredArgumentBuilder<CommandSourceStack, T>> builder1 = RequiredArgumentBuilder.argument(name, type);
		builder1.redirect(root, infoSupplier::apply);
		builder.then(builder1);
		root.addChild(builder.build());
	}
}
