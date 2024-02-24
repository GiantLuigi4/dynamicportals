package tfc.dynamicportals.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.api.registry.PortalTypes;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class DynamicPortalsCommand {
//	private static final Map<String, Function<UUID, BasicPortal>> portalCreators = new HashMap<>();
//
//	static {
//		portalCreators.put("basic", BasicCommandPortal::new);
//		portalCreators.put("nether", BasicNetherPortal::new);
//		portalCreators.put("end", BasicEndPortal::new);
//		portalCreators.put("mirror", BasicMirror::new);
//	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> build(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = literal("dynamicportals");
		builder.executes(context -> {
			context.getSource().sendSuccess(() -> Component.translatable("dynamicportals.command.bread.help"), true);
			return 0;
		});
		CommandNode<CommandSourceStack> commandNode = dispatcher.register(builder);
		buildSubcommand("create", null, commandNode, context -> {
			DynamicPortalsSourceStack ctx;
			if (context.getSource() instanceof DynamicPortalsSourceStack)
				ctx = (DynamicPortalsSourceStack) context.getSource();
			else {
				context.getSource().sendFailure(Component.translatable("dynamicportals.command.cheese.missing_args"));
				return -1;
			}
			ServerLevel lvl = context.getSource().getLevel();
			NetworkHolder netHolder = (NetworkHolder) lvl;
			
			Vec3 sizeVec = ctx.getPositionFromWorldCoordinates("size");
//			if (sizeVec == null) {
//				context.getSource().sendFailure(Component.translatable("dynamicportals.command.cheese.missing_size"));
//				return -1;
//			}
			PortalFilter targetFilter = ctx.getArgument("target", PortalFilter.class);
			Vec3 position = ctx.getPositionFromWorldCoordinatesOrDefault("position", context.getSource().getPosition());
			ResourceLocation type = ctx.getArgumentOrDefault("type", ResourceLocation.class, new ResourceLocation("dynamicportals", "basic"));
			
			Vec3 rotation = ctx.getPositionFromWorldCoordinatesOrDefault("rotation", Vec3.directionFromRotation(context.getSource().getRotation()));
			Vec3 normal = ctx.getPositionFromWorldCoordinates("normal");
			boolean frontOnly = ctx.getArgumentOrDefault("frontonly", Boolean.class, false);
			
			CompoundTag tag = new CompoundTag();
			tag.putLongArray("coords",
					new long[]{
							Double.doubleToLongBits(position.x),
							Double.doubleToLongBits(position.y),
							Double.doubleToLongBits(position.z)
					});
			tag.putUUID("uuid", ctx.getArgumentOrDefault("uuid", UUID.class, new UUID(System.nanoTime(), lvl.getGameTime())));
			
			AbstractPortal newPortal = PortalTypes.createPortal(type, netHolder, tag);
			System.out.println(newPortal.getPosition());
//			BasicPortal newPortal = portalCreators
//					                        .get(ctx.getArgumentOrDefault("type", String.class, "basic"))
//					                        .apply()
//					                        .setPosition()
//					                        .setSize(sizeVec.x, sizeVec.z)
//					                        .setRotation(VecMath.toRadians(ctx.getPositionFromWorldCoordinatesOrDefault("rotation", new Vec3(context.getSource().getRotation().y, -context.getSource().getRotation().x, 0))));
//			if (normal != null) newPortal.setRenderNormal(normal);
//			if () newPortal.computeRenderNormal();
			
			if (targetFilter != null) {
				List<AbstractPortal> possibleTargets = new ArrayList<>();
				netHolder.getPortalNetworks().forEach((net) -> possibleTargets.addAll(List.of(targetFilter.apply(net.getPortals(), context))));
				if (!possibleTargets.isEmpty()) {
					AbstractPortal target = possibleTargets.get(0);
					target.getConnectedNetwork().link(newPortal);
				} else {
					ctx.sendFailure(Component.translatable("dynamicportals.command.cheese.invalid_target"));
				}
			} else {
				PortalNet newNet = new PortalNet(UUID.randomUUID());
				newNet.link(newPortal);
				netHolder.getPortalNetworks().add(newNet);
			}
			
//			int newId = Temp.addPortal(context.getSource().getLevel(), (CommandPortal) newPortal);
//			if (newPortal.target == newPortal)
//				ctx.sendSuccess(() -> Component.translatable("dynamicportals.command.bread.mirror", newId), true);
//			else
			ctx.sendSuccess(() -> Component.translatable("dynamicportals.command.bread.target", newPortal.getUUID(), newPortal.getConnectedNetwork().getUUID()), true);
			return 0;
		});
		
		buildSubcommand("delete", PortalSelectorArgument.create(), commandNode, context -> {
			final AtomicReference<PortalFilter> portalFilter = new AtomicReference<>();
			try {
				portalFilter.set(context.getArgument("delete", PortalFilter.class));
			} catch (Throwable ignored) {
			}
			if (portalFilter.get() == null) {
				context.getSource().sendFailure(Component.translatable("dynamicportals.command.cheese.empty_argument"));
				return -1;
			}
			
			final AtomicInteger count = new AtomicInteger(0);
			NetworkHolder netHolder = (NetworkHolder) context.getSource().getLevel();
			netHolder.getPortalNetworks().forEach((net) -> {
				for (AbstractPortal portal : portalFilter.get().apply(net.getPortals(), context)) {
					portal.getConnectedNetwork().unlink(portal);
					count.incrementAndGet();
				}
			});
			
			context.getSource().sendSuccess(() -> Component.translatable("dynamicportals.command.bread.delete", count.get()), true);
			return count.get();
		});
		
//		buildSubcommand("modify", PortalSelectorArgument.create(), commandNode, context -> {
//			DynamicPortalsSourceStack ctx;
//			if (context.getSource() instanceof DynamicPortalsSourceStack)
//				ctx = (DynamicPortalsSourceStack) context.getSource();
//			else {
//				context.getSource().sendFailure(Component.translatable("dynamicportals.command.cheese.modify_missing_args"));
//				return -1;
//			}
//
//			PortalFilter filter = null;
//			try {
//				filter = context.getArgument("modify", PortalFilter.class);
//			} catch (Throwable ignored) {
//			}
//			if (filter == null) {
//				context.getSource().sendFailure(Component.translatable("dynamicportals.command.cheese.empty_argument"));
//				return -1;
//			}
//
//			int count = 0;
//			Map<BasicPortal, UUID> toRemove = new HashMap<>();
//			Map<BasicPortal, UUID> targets = new HashMap<>();
//			for (AbstractPortal commandPortal : Temp.filter(context.getSource().getLevel(), filter, context)) {
//				if (commandPortal instanceof BasicPortal bap) {
//					String type;
//					if ((type = ctx.getArgument("type", String.class)) != null) {
//						if (!type.equals(bap.type.toString())) {
//							UUID old = bap.getUUID();
//							UUID oldTarget = bap.target.uuid;
//							bap = portalCreators.get(type).apply(bap.uuid).setPosition(bap.raytraceOffset()).setSize(bap.getSize()).setRotation(bap.getRotation());
//							toRemove.put(bap, old);
//							targets.put(bap, oldTarget);
//						}
//					}
//
//					bap.setPosition(ctx.getPositionFromWorldCoordinatesOrDefault("position", bap.raytraceOffset()));
//
//					Vec3 vec = ctx.getPositionFromWorldCoordinatesOrDefault("rotation", null);
//					if (vec != null) bap.setRotation(VecMath.toRadians(vec));
//
//					PortalFilter targetFilter = ctx.getArgument("target", PortalFilter.class);
//					if (targetFilter != null) {
//						CommandPortal[] possibleTargets = Temp.filter(context.getSource().getLevel(), targetFilter, context);
//						if (possibleTargets.length > 0) {
//							CommandPortal target = possibleTargets[0];
//							((AbstractPortal) target).setTarget(bap);
//							bap.setTarget((AbstractPortal) target);
//						} else {
//							ctx.sendFailure(Component.translatable("dynamicportals.command.cheese.invalid_target"));
//						}
//					}
//
//					Vec3 sizeVec = ctx.getPositionFromWorldCoordinates("size");
//					if (sizeVec != null) bap.setSize(sizeVec.x, sizeVec.z);
//
//					Vec3 normalVec = ctx.getPositionFromWorldCoordinates("normal");
//					if (normalVec != null) bap.setRenderNormal(normalVec);
//					if (ctx.getArgumentOrDefault("frontonly", String.class, "false").equals("true")) bap.computeRenderNormal();
//				}
//
//				count++;
//			}
//			for (Map.Entry<BasicPortal, UUID> entry : toRemove.entrySet()) {
//				Temp.remove(context.getSource().getLevel(), entry.getValue());
//				Temp.addPortal(context.getSource().getLevel(), entry.getKey());
//			}
//			for (Map.Entry<BasicPortal, UUID> entry : targets.entrySet()) {
//				Optional<AbstractPortal> portal = Arrays.stream(Temp.getPortals(context.getSource().getLevel())).filter(p -> p.uuid == entry.getValue()).findFirst();
//				portal.ifPresent(abstractPortal -> entry.getKey().setTarget(abstractPortal));
//			}
//
//			context.getSource().sendSuccess(() -> Component.translatable("dynamicportals.command.bread.update", count), true);
//			return count;
//		});
		
		buildRedirectedSubcommand("position", Vec3Argument.vec3(false), commandNode);
		buildRedirectedSubcommand("rotation", Vec3Argument.vec3(false), commandNode);
		buildRedirectedSubcommand("size", Vec2Argument.vec2(false), commandNode);
		buildRedirectedSubcommand("normal", Vec3Argument.vec3(false), commandNode);
		buildRedirectedSubcommand("type", ResourceLocationArgument.id(), commandNode);
		buildRedirectedSubcommand("frontonly", BoolArgumentType.bool(), commandNode);
		buildRedirectedSubcommand("uuid", UuidArgument.uuid(), commandNode);
		buildRedirectedSubcommand("target", PortalSelectorArgument.create(), commandNode);
		return builder;
	}
	
	// jank hack to forward information to the redirects
	private static CommandSourceStack toSource(CommandContext<CommandSourceStack> context) {
		int perm = 0;
		while (context.getSource().hasPermission(perm)) perm++;
		return new DynamicPortalsSourceStack(context, --perm);
	}
	
	// give me something to work with, and I will butcher it until it works in a way which is easy to work with
	// :GWchadThink: epic luigi
	// lorenzo: I have created the ultimate building function
	private static <T> void buildSubcommand(String name, ArgumentType<T> type, CommandNode<CommandSourceStack> command, Command<CommandSourceStack> cmd) {
		ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> subCommand = LiteralArgumentBuilder.literal(name);
		if (type != null) {
			ArgumentBuilder<CommandSourceStack, RequiredArgumentBuilder<CommandSourceStack, T>> argument = RequiredArgumentBuilder.argument(name, type);
			if (cmd == null) {
				argument.redirect(command, DynamicPortalsCommand::toSource);
				cmd = context -> {
					StringReader reader = new StringReader(context.getInput());
					reader.setCursor(context.getInput().length());
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(reader);
				};
			}
			argument.executes(cmd);
			subCommand.then(argument);
		}
		subCommand.executes(cmd);
		command.addChild(subCommand.build());
	}
	
	private static <T> void buildRedirectedSubcommand(String name, ArgumentType<T> type, CommandNode<CommandSourceStack> command) {
		buildSubcommand(name, type, command, null);
	}
}