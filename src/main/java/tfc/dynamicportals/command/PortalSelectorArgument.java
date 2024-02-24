package tfc.dynamicportals.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.arguments.selector.EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE;
import static net.minecraft.commands.arguments.selector.options.EntitySelectorOptions.ERROR_UNKNOWN_OPTION;

public class PortalSelectorArgument implements ArgumentType<PortalFilter> {
	public List<String> options = List.of("uuid", "id", "type");
	
	public PortalSelectorArgument() {
	}
	
	public static PortalSelectorArgument create() {
		return new PortalSelectorArgument();
	}
	
	@Override
	public PortalFilter parse(StringReader reader) throws CommandSyntaxException {
		if (reader.canRead()) {
			if (reader.peek() == '@') {
				reader.skip();
				if (reader.canRead()) {
					if (reader.peek() == '[') {
						reader.skip();
						List<String[]> selectors = new ArrayList<>();
						while (reader.canRead() && reader.peek() != ']') {
							selectors.add(parseSingleFilter(reader));
							if (reader.canRead() && reader.peek() == ',') reader.skip();
						}
						if (reader.canRead() && reader.peek() == ']') {
							reader.skip();
							PortalFilter filter = (portals, ctx) -> portals.toArray(new AbstractPortal[0]);
							for (String[] split : selectors) {
								PortalFilter oldFilter = filter;
								filter = (portals, ctx) -> {
									ArrayList<AbstractPortal> output = new ArrayList<>();
									for (AbstractPortal portal : oldFilter.apply(portals, ctx)) {
										switch (split[0]) {
											case "type" -> {
												if (portal.type.getRegistryName().getPath().equals(split[1])) output.add(portal);
											}
//											case "id" -> {
//												try {
//													if (portal.myId() == Integer.parseInt(split[1])) output.add(portal);
//												} catch (Throwable ignored) {
//													//TODO: deal with erroneous behaviour?
//												}
//											}
											case "uuid" -> {
												if (portal.getUUID().toString().equals(split[1])) output.add(portal);
											}
										}
									}
									return output.toArray(new AbstractPortal[0]);
								};
							}
							return filter;
						} else {
							throw new SimpleCommandExceptionType(Component.translatable("dynamicportals.command.cheese.invalid_argument")).createWithContext(reader);
						}
					} else if (reader.peek() == ' ') {
						return (portals, context) -> portals.toArray(new AbstractPortal[0]);
					} else {
						throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(reader);
					}
				} else {
					return (portals, context) -> portals.toArray(new AbstractPortal[0]);
				}
			}
//			else {
//				String r = reader.readString();
//				try {
//					int id = Integer.parseInt(r);
//					return (portals, context) -> {
//						for (AbstractPortal portal : portals)
//							if (portal.myId() == id)
//								return new AbstractPortal[]{portal};
//						return new AbstractPortal[0];
//					};
//				} catch (Throwable ignored) {
//				}
//				throw new SimpleCommandExceptionType(Component.translatable("dynamicportals.command.cheese.invalid_simple_id", r)).createWithContext(reader);
//			}
		} else {
			throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(reader);
		}
		return ((portals, ctx) -> null);
	}
	
	public String[] parseSingleFilter(StringReader reader) throws CommandSyntaxException {
		String option = reader.readString();
		if (option.isEmpty()) throw new SimpleCommandExceptionType(Component.translatable("dynamicportals.command.cheese.empty_option")).createWithContext(reader);
		else if (!options.contains(option)) throw ERROR_UNKNOWN_OPTION.createWithContext(reader, option);
		if (reader.peek() == '=') {
			reader.skip();
			String argument = reader.readString();
			if (argument.isEmpty()) throw new SimpleCommandExceptionType(Component.translatable("dynamicportals.command.cheese.empty_argument")).createWithContext(reader);
			return new String[]{option, argument};
		} else throw new SimpleCommandExceptionType(Component.translatable("dynamicportals.command.cheese.missing_equal")).createWithContext(reader);
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (context.getSource() instanceof SharedSuggestionProvider) {
			String selector = builder.getRemaining();
			ArrayList<AbstractPortal> portals = new ArrayList<>();
			//TODO: uhm idk if I should reference the freaking Minecraft class lmao
			((NetworkHolder) Minecraft.getInstance().level).getPortalNetworks().forEach((net) -> portals.addAll(net.getPortals()));
			if (selector.equals("@")) {
				return SharedSuggestionProvider.suggest(new String[]{"@["}, builder);
			} else if (selector.startsWith("@[")) {
				int selectorStart = Math.max(selector.indexOf("["), selector.lastIndexOf(","));
				int selectorEnd = Math.max(selector.lastIndexOf(","), selector.lastIndexOf("="));
				
				String selectorType = selector.substring(selectorStart + 1);
				String argument = selector.substring((selectorEnd >= 0 ? selectorEnd : selectorStart) + 1);
				List<String> options = new ArrayList<>();
				
				if (selectorStart < selectorEnd) {
					if (selectorType.startsWith("type=")) {
//						options.addAll(List.of());
					} else if (selectorType.startsWith("uuid=")) {
						for (AbstractPortal p : portals)
							options.add(p.getUUID().toString());
					}
//					else if (selectorType.startsWith("id=")) {
//						for (AbstractPortal p : portals)
//							if (p instanceof AbstractPortal)
//								options.add(Integer.toString(((AbstractPortal) p).myId()));
//					}
				} else {
					for (String o : this.options)
						options.add(o + "=");
				}
				
				for (String option : options) {
					if (option.equals(argument)) {
						return SharedSuggestionProvider.suggest(new String[]{",", "]"}, builder.createOffset(builder.getStart() + builder.getRemaining().length()));
					}
				}
				
				return SharedSuggestionProvider.suggest(options, builder.createOffset(builder.getStart() + (selectorEnd < 0 ? selectorStart : selectorEnd) + 1));
			}
			List<String> ids = new ArrayList<>(List.of("@"));
			for (AbstractPortal p : portals) {
				ids.add(p.getUUID().toString());
			}
			return SharedSuggestionProvider.suggest(ids, builder);
		} else {
			return Suggestions.empty();
		}
	}
	
	@Override
	public Collection<String> getExamples() {
		return List.of("@", "@[uuid=586ca6a2-ad52-4b4e-8e95-2222ae39cb7a]", "@[type=basic]");
	}
}
