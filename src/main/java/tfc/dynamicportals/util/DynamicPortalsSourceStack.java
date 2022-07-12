package tfc.dynamicportals.util;

import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DynamicPortalsSourceStack extends CommandSourceStack {
	public final String input;
	public final List<ParsedCommandNode<CommandSourceStack>> nodes;
	public final CommandContext<CommandSourceStack> context;
	
	public DynamicPortalsSourceStack(CommandSource pSource, Vec3 pWorldPosition, Vec2 pRotation, ServerLevel pLevel, int pPermissionLevel, String pTextName, Component pDisplayName, MinecraftServer pServer, @Nullable Entity pEntity, boolean pSilent, @Nullable ResultConsumer<CommandSourceStack> pConsumer, EntityAnchorArgument.Anchor pAnchor, String input, List<ParsedCommandNode<CommandSourceStack>> nodes, CommandContext<CommandSourceStack> context) {
		super(pSource, pWorldPosition, pRotation, pLevel, pPermissionLevel, pTextName, pDisplayName, pServer, pEntity, pSilent, pConsumer, pAnchor);
		this.input = input;
		this.nodes = nodes;
		this.context = context;
	}
	
	public <T> T getArgument(String name, Class<T> clazz) {
		return getArgumentOrDefault(name, clazz, null);
	}
	
	public <T> T getArgumentOrDefault(String name, Class<T> clazz, T def) {
		T v = null;
		try {
			v = context.getArgument(name, clazz);
		} catch (Throwable ignored) {
			if (context.getSource() instanceof DynamicPortalsSourceStack) {
				v = ((DynamicPortalsSourceStack) context.getSource()).getArgumentOrDefault(name, clazz, def);
			}
		}
		return v == null ? def : v;
	}
	
	public Vec3 getPositionFromWorldCoordinates(String name) {
		return getPositionFromWorldCoordinatesOrDefault(name, null);
	}
	
	public Vec3 getPositionFromWorldCoordinatesOrDefault(String name, Vec3 def) {
		WorldCoordinates v = getArgument(name, WorldCoordinates.class);
		return v == null ? def : v.getPosition(this);
	}
}
