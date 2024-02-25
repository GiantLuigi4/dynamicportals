package tfc.dynamicportals.command.arg;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.LocalCoordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.commands.SharedSuggestionProvider.suggestCoordinates;

public class OrientationArgument extends Vec3Argument {
	public OrientationArgument() {
		super(false);
	}
	
	public static OrientationArgument vec3() {
		return new OrientationArgument();
	}
	
	public static Vec3 getRotation(CommandContext<CommandSourceStack> pContext, String pName) {
		Coordinates crds = pContext.getArgument(pName, Coordinates.class);
		Vec3 vec = crds.getPosition(pContext.getSource());
		
		double xc;
		if (crds.isXRelative()) xc = pContext.getSource().getRotation().x;
		else xc = vec.x;
		
		double yc;
		if (crds.isXRelative()) yc = pContext.getSource().getRotation().y;
		else yc = vec.y;
		
		double zc;
		if (crds.isXRelative()) zc = 0;
		else zc = vec.z;
		
		return new Vec3(xc, yc, zc);
	}
	
	public Coordinates parse(StringReader p_120843_) throws CommandSyntaxException {
		return (WorldCoordinates.parseDouble(p_120843_, false));
	}
	
	protected double roundTo(double value, double points) {
		double rv;
		if (points == 0) rv = 0;
		else if (points == 1) rv = 10;
		else if (points == 2) rv = 100;
		else rv = Math.pow(10, points);
		
		return Math.round(value * rv) / rv;
	}
	
	protected String ensurePoints(String from, int count) {
		if (!from.contains("."))
			return from + "." + "0".repeat(count);
		else {
			String[] split = from.split("\\.", 2);
			return split[0] + "." + split[1] + "0".repeat(count - split[1].length());
		}
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
		if (!(pContext.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			String s = pBuilder.getRemaining();
			Collection<SharedSuggestionProvider.TextCoordinates> collection = new ArrayList<>();
			if (pContext.getSource() instanceof ClientSuggestionProvider) {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player != null) {
					collection.add(new SharedSuggestionProvider.TextCoordinates(
							ensurePoints(String.valueOf(roundTo(mc.player.getXRot(), 2)), 2),
							ensurePoints(String.valueOf(roundTo(mc.player.getYRot(), 2)), 2),
							"0.00"
					));
				}
			} else {
				System.out.println(pContext.getSource().getClass());
			}
			
			return suggestCoordinates(s, collection, pBuilder, Commands.createValidator(this::parse));
		}
	}
}
