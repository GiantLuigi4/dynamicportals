package tfc.dynamicportals.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.api.registry.PortalTypes;
import tfc.dynamicportals.command.arg.OrientationArgument;
import tfc.dynamicportals.command.registry.PortalTypeCommands;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;


public class DynamicPortalsCommand {
	
	//new ResourceLocation("dynamicportals", "basic"), context.getSource().getPosition(), new Vec2(2, 2), Vec3.directionFromRotation(context.getSource().getRotation())
	public static int createStandardPortal(PortalType<?> type, CommandContext<CommandSourceStack> context) {
		Vec3 position = getArgumentOrDefault(context, "position", Vec3.class, context.getSource().getPosition());
		Vec2 size = getArgumentOrDefault(context, "size", Vec2.class, new Vec2(2, 2));
		Vec3 rotation = OrientationArgument.getRotation(context, "rotation");
		Boolean doubleSided = getArgumentOrDefault(context, "double_sided", Boolean.class, true);
		
		ServerLevel lvl = context.getSource().getLevel();
		NetworkHolder netHolder = (NetworkHolder) lvl;
		Optional<PortalNet> optionalNet = netHolder.getPortalNetworks().stream().filter((net) -> net.getUUID().equals(UuidArgument.getUuid(context, "portal_net"))).findFirst();
		CompoundTag levelTag = new CompoundTag();
		levelTag.putString("registry", lvl.dimension().registry().toString());
		levelTag.putString("location", lvl.dimension().location().toString());
		CompoundTag tag = new CompoundTag();
		tag.put("level", levelTag);
		// position
		tag.putLongArray("coords",
				new long[]{
						Double.doubleToLongBits(position.x),
						Double.doubleToLongBits(position.y),
						Double.doubleToLongBits(position.z)
				});
		
		// size
		tag.putLongArray("size",
				new long[]{
						Double.doubleToLongBits(size.x),
						Double.doubleToLongBits(size.y)
				});
		
		// orientation
		Quaterniond orientation = new Quaterniond();
		orientation.rotateAxis(rotation.x, 1, 0, 0);
		orientation.rotateAxis(rotation.y, 0, 1, 0);
		orientation.rotateAxis(rotation.z, 0, 0, 1);
		tag.putLongArray("orientation",
				new long[]{
						Double.doubleToLongBits(orientation.x),
						Double.doubleToLongBits(orientation.y),
						Double.doubleToLongBits(orientation.z),
						Double.doubleToLongBits(orientation.w)
				});
		
		tag.putBoolean("double_sided", doubleSided);
		
		AbstractPortal newPortal = PortalTypes.createPortal(type.getRegistryName(), netHolder, tag);
		
		PortalNet newNet = optionalNet.orElseGet(() -> new PortalNet(UUID.randomUUID()));
		newNet.link(newPortal);
		if (optionalNet.isEmpty()) {
			netHolder.getPortalNetworks().add(newNet);
			context.getSource().sendSuccess(() -> Component.literal("Successfully created portal net with UUID %s and portal with local id 0.".formatted(newNet.getUUID())), false);
		} else {
			context.getSource().sendSuccess(() -> Component.literal("Successfully created and added portal with local id %d to portal net with UUID %s.".formatted(newNet.getPortals().size() - 1, newNet.getUUID())), false);
		}
		return 1;
	}
	
	public static <T> T getArgumentOrDefault(CommandContext<CommandSourceStack> context, String arg, Class<T> argTypeClass, T defaultOption) {
		T argValue = null;
		try {
			argValue = context.getArgument(arg, argTypeClass);
		} catch (Throwable ignored) {}
		return argValue == null ? defaultOption : argValue;
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		LiteralArgumentBuilder<CommandSourceStack> dynamicportalsBuilder = literal("dynamicportals").executes(context -> {
			context.getSource().sendSuccess(() -> Component.translatable("dynamicportals.command.bread.help"), true);
			return 0;
		}).then(argument("portal_net", UuidArgument.uuid()).suggests((context, suggestionsBuilder) -> {
			ServerLevel lvl = context.getSource().getLevel();
			NetworkHolder netHolder = (NetworkHolder) lvl;
			List<String> uuids = new ArrayList<>();
			netHolder.getPortalNetworks().forEach((net) -> uuids.add(net.getUUID().toString()));
			return SharedSuggestionProvider.suggest(uuids, suggestionsBuilder);
		}).then(
				PortalTypeCommands.fill(literal("create"))
		).then(
				literal("destroy")
						.executes((ctx) -> {
							ServerLevel lvl = ctx.getSource().getLevel();
							NetworkHolder netHolder = (NetworkHolder) lvl;
							Optional<PortalNet> optionalNet = netHolder.getPortalNetworks().stream().filter((net) -> net.getUUID().equals(UuidArgument.getUuid(ctx, "portal_net"))).findFirst();
							if (optionalNet.isPresent()) {
								netHolder.getPortalNetworks().remove(optionalNet.get());
								ctx.getSource().sendSuccess(() -> Component.literal("Successfully destroyed network %s.".formatted(optionalNet.get().getUUID())), false);
								return 1;
							}
							ctx.getSource().sendSuccess(() -> Component.literal("Network no exist!"), false);
							return 0;
						})
		));
		
		return dynamicportalsBuilder;
	}
}