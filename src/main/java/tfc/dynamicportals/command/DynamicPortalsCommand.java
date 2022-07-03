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
import java.util.UUID;
import java.util.function.Function;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class DynamicPortalsCommand {
	private static final HashMap<String, Function<UUID, BasicPortal>> portalCreators = new HashMap<>();
	
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
			BasicPortal portal = portalCreators.get(type).apply(uuid);
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
				// TODO: fix this (x rotation is backwards)
				rotation = new Vec3(Math.toRadians(rotato.y), Math.toRadians(-rotato.x), 0);
			}
			Vec3 normal;
			try {
				normal = norm.getPosition(ctx);
			} catch (Throwable ignored) {
				normal = null;
			}
			portal.setPosition(vec.x, vec.y, vec.z);
			Vec2d sizeVec;
			try {
				Vec3 vec1 = size.getPosition(ctx);
				sizeVec = new Vec2d(vec1.x, vec1.z);
			} catch (Throwable ignored) {
				context.getSource().sendFailure(new TranslatableComponent("dynamicportals.command.cheese.size_crab"));
				return -1;
			}
			portal.setSize(sizeVec.x, sizeVec.y);
			portal.setRotation(rotation.x, rotation.y, rotation.z);
			if (normal != null) {
				portal.setNormal(normal);
			}
			try {
				if (ctx.getArgument("frontonly", String.class).equals("true"))
					portal.computeNormal();
			} catch (Throwable ignored) {
			}
			boolean log = true;
			if (context.getSource().hasPermission(4)) {
				if (context.getSource().getLevel().getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
					log = false;
				}
			}
			int v = Temp.addPortal(context.getSource().getLevel(), (CommandPortal) portal);
			ctx.sendSuccess(new TranslatableComponent("dynamicportals.command.bread.id", v), log);
			FullPortalFilter i = ctx.getArgument("target", FullPortalFilter.class);
			if (i != null) {
				CommandPortal portal1 = Temp.filter(i, context)[0];
				((AbstractPortal) portal1).target = portal;
				portal.target = (AbstractPortal) portal1;
			}
			if (portal.target == portal)
				ctx.sendSuccess(new TranslatableComponent("dynamicportals.command.bread.mirror", v), log);
			else ctx.sendSuccess(new TranslatableComponent("dynamicportals.command.bread.target", v), log);
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
			ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> builder2 = LiteralArgumentBuilder.literal("delete");
			ArgumentBuilder<CommandSourceStack, RequiredArgumentBuilder<CommandSourceStack, FullPortalFilter>> builder1 = RequiredArgumentBuilder.argument("target", PortalSelectorArgument.create());
			Command<CommandSourceStack> exec = context -> {
				FullPortalFilter i = context.getArgument("target", FullPortalFilter.class);
				int count = 0;
				for (CommandPortal commandPortal : Temp.filter(i, context)) {
					Temp.remove(commandPortal.myId());
					count += 1;
				}
				boolean log = true;
				if (context.getSource().hasPermission(4)) {
					if (context.getSource().getLevel().getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
						log = false;
					}
				}
				context.getSource().sendSuccess(new TranslatableComponent("dynamicportals.command.bread.delete", count), log);
				return count;
			};
			builder2.executes(exec);
			builder1.executes(exec);
			builder2.then(builder1);
			commandNode.addChild(builder2.build());
		}
		
		return builder;
	}
	
	// jank hack to forward information to the redirects
	private static CommandSourceStack toSource(CommandContext<CommandSourceStack> context) {
		int perm = 0;
		while (context.getSource().hasPermission(perm)) perm++;
		DynamicPortalsSourceStack sourceStack = new DynamicPortalsSourceStack(
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
		return sourceStack;
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
