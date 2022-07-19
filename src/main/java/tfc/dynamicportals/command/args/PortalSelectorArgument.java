package tfc.dynamicportals.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.command.FullPortalFilter;
import tfc.dynamicportals.command.portals.CommandPortal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.arguments.selector.EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE;
import static net.minecraft.commands.arguments.selector.options.EntitySelectorOptions.ERROR_UNKNOWN_OPTION;

public class PortalSelectorArgument implements ArgumentType<FullPortalFilter> {
	public List<String> options = List.of("uuid", "id", "type");
	
	public PortalSelectorArgument() {
	}
	
	public static PortalSelectorArgument create() {
		return new PortalSelectorArgument();
	}
	
	public FullPortalFilter parse(StringReader reader) throws CommandSyntaxException {
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
							FullPortalFilter filter = (portals, ctx) -> portals.toArray(new CommandPortal[0]);
							for (String[] split : selectors) {
								FullPortalFilter oldFilter = filter;
								filter = (portals, ctx) -> {
									ArrayList<CommandPortal> output = new ArrayList<>();
									for (CommandPortal portal : oldFilter.filter(portals, ctx)) {
										switch (split[0]) {
											case "type" -> {
												if (portal.type().equals(split[1])) {
													output.add(portal);
												}
											}
											case "id" -> {
												if (portal.myId() == Integer.parseInt(split[1])) {
													output.add(portal);
												}
											}
											case "uuid" -> {
												if (((AbstractPortal) portal).uuid.toString().equals(split[1])) {
													output.add(portal);
												}
											}
										}
									}
									return output.toArray(new CommandPortal[0]);
								};
							}
							return filter;
						} else {
							throw new SimpleCommandExceptionType(new TranslatableComponent("dynamicportals.command.cheese.invalid_argument")).createWithContext(reader);
						}
					} else if (reader.peek() == ' ') {
						return (portals, context) -> portals.toArray(new CommandPortal[0]);
					} else {
						throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(reader);
					}
				} else {
					return (portals, context) -> portals.toArray(new CommandPortal[0]);
				}
			} else {
				String r = reader.readString();
				try {
					int id = Integer.parseInt(r);
					return (portals, context) -> {
						for (CommandPortal portal : portals)
							if (portal.myId() == id)
								return new CommandPortal[]{portal};
						return new CommandPortal[0];
					};
				} catch (Throwable ignored) {
				}
				throw new SimpleCommandExceptionType(new TranslatableComponent("dynamicportals.command.cheese.invalid_simple_id", r)).createWithContext(reader);
			}
		} else {
			throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(reader);
		}
	}
	
	public String[] parseSingleFilter(StringReader reader) throws CommandSyntaxException {
		String option = reader.readString();
		if (option.isEmpty())
			throw new SimpleCommandExceptionType(new TranslatableComponent("dynamicportals.command.cheese.empty_option")).createWithContext(reader);
		else if (!options.contains(option))
			throw ERROR_UNKNOWN_OPTION.createWithContext(reader, option);
		if (reader.peek() == '=') {
			reader.skip();
			String argument = reader.readString();
			if (argument.isEmpty())
				throw new SimpleCommandExceptionType(new TranslatableComponent("dynamicportals.command.cheese.empty_argument")).createWithContext(reader);
			return new String[]{option, argument};
		} else {
			throw new SimpleCommandExceptionType(new TranslatableComponent("dynamicportals.command.cheese.missing_equal")).createWithContext(reader);
		}
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
		if (pContext.getSource() instanceof SharedSuggestionProvider) {
			String selector = pBuilder.getRemaining();
			AbstractPortal[] portals = Temp.getPortals(null); //TODO: not actually null, but for now it's ok
			if (selector.equals("@")) {
				return SharedSuggestionProvider.suggest(new String[]{"@["}, pBuilder);
			} else if (selector.startsWith("@[")) {
				int selectorStart = Math.max(selector.indexOf("["), selector.lastIndexOf(","));
				int selectorEnd = Math.max(selector.lastIndexOf(","), selector.lastIndexOf("="));
				
				String selectorType = selector.substring(selectorStart + 1);
				String argument = selector.substring((selectorEnd >= 0 ? selectorEnd : selectorStart) + 1);
				List<String> options = new ArrayList<>();
				
				if (selectorStart < selectorEnd) {
					if (selectorType.startsWith("type=")) {
						options.addAll(List.of("basic", "nether", "end"));
					} else if (selectorType.startsWith("uuid=")) {
						for (AbstractPortal p : portals)
							options.add(p.uuid.toString());
					} else if (selectorType.startsWith("id=")) {
						for (AbstractPortal p : portals)
							if (p instanceof CommandPortal)
								options.add(Integer.toString(((CommandPortal) p).myId()));
					}
				} else {
					for (String o : this.options)
						options.add(o + "=");
				}
				
				for (String option : options) {
					if (option.equals(argument)) {
						return SharedSuggestionProvider.suggest(new String[]{",", "]"}, pBuilder.createOffset(pBuilder.getStart() + pBuilder.getRemaining().length()));
					}
				}
				
				return SharedSuggestionProvider.suggest(options, pBuilder.createOffset(pBuilder.getStart() + (selectorEnd < 0 ? selectorStart : selectorEnd) + 1));
			}
			List<String> ids = new ArrayList<>(List.of("@"));
			for (AbstractPortal p : portals) {
				if (p instanceof CommandPortal)
					ids.add(Integer.toString(((CommandPortal) p).myId()));
			}
			return SharedSuggestionProvider.suggest(ids, pBuilder);
		} else {
			return Suggestions.empty();
		}
	}
	
	public Collection<String> getExamples() {
		return List.of("0", "10", "@", "@[uuid=586ca6a2-ad52-4b4e-8e95-2222ae39cb7a]", "@[id=0]", "@[type=basic]");
	}
}
