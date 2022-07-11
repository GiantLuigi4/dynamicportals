package tfc.dynamicportals.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.command.CommandPortal;
import tfc.dynamicportals.command.FullPortalFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PortalSelectorArgument implements ArgumentType<FullPortalFilter> {
	public PortalSelectorArgument() {
	}
	
	public static PortalSelectorArgument create() {
		return new PortalSelectorArgument();
	}
	
	public FullPortalFilter parse(StringReader reader) throws CommandSyntaxException {
		// TODO: check
		if (reader.canRead()) {
			String selector = "";
			while (reader.canRead()) {
				if (reader.peek() == ' ') break;
				selector += reader.read();
			}
			System.out.println(selector);
			try {
				int id = Integer.parseInt(selector);
				return (portals, context) -> {
					for (CommandPortal portal : portals)
						if (portal.myId() == id)
							return new CommandPortal[]{portal};
					return null;
				};
			} catch (Throwable ignored) {
			}
			if (selector.equals("@")) return (portals, context) -> portals.toArray(new CommandPortal[0]);
			// TODO: actual selectors
			else if (selector.startsWith("@[") && selector.endsWith("]")) {
				selector = selector.substring(2, selector.length() - 1);
				String[] selectors = selector.split(",");
				int selCount = 0;
				for (char c : selector.toCharArray()) {
					if (c == ',') selCount++;
				}
				FullPortalFilter filter = (portals, ctx) -> portals.toArray(new CommandPortal[0]);
				boolean success = false;
				if (selectors.length == (selCount + 1)) {
					for (String arg : selectors) {
						String[] split = arg.split("=", 2);
						if (split.length != 2) throw new CommandSyntaxException(null, new TranslatableComponent("dynamicportals.command.cheese.equal"));
						
						success = true;
						
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
				}
				if (success) return filter;
				
				throw new CommandSyntaxException(null, new TranslatableComponent("dynamicportals.command.cheese.comma"));
			}
			throw new CommandSyntaxException(null, new TranslatableComponent("dynamicportals.command.cheese.unknown"));
		}
		throw new CommandSyntaxException(null, new TranslatableComponent("dynamicportals.command.cheese.maybe_empty"));
	}
	
	//lorenzo: cleaned up a bit+added selectors :D
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
		if (pContext.getSource() instanceof SharedSuggestionProvider) {
			String selector = pBuilder.getRemaining();
			AbstractPortal[] portals = Temp.getPortals(null); //TODO: not actually null, but for now it's ok
			if (selector.equals("@")) {
				return SharedSuggestionProvider.suggest(new String[]{"@["}, pBuilder);
			} else if (selector.startsWith("@[")) {
				int lastStart = Math.max(selector.indexOf("["), selector.lastIndexOf(","));
				int lastEnd = Math.max(selector.lastIndexOf(","), selector.lastIndexOf("="));
				SuggestionsBuilder builder = pBuilder.createOffset(pBuilder.getStart() + (lastEnd < 0 ? lastStart : lastEnd) + 1);
				
				String selectorType = selector.substring(lastStart + 1);
				String argument = selector.substring((lastEnd < 0 ? lastStart : lastEnd) + 1);
				
				List<String> options = new ArrayList<>();
				
				if (selector.lastIndexOf(",") < selector.lastIndexOf("=")) {
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
					options.addAll(List.of("uuid=", "id=", "type="));
				}
				
				for (String option : options) {
					if (option.equals(argument)) {
						return SharedSuggestionProvider.suggest(new String[]{",", "]"}, pBuilder.createOffset(pBuilder.getStart() + pBuilder.getRemaining().length()));
					} else if (option.startsWith(argument)) {
						builder.suggest(option);
					}
				}
				
				pBuilder.add(builder);
				return pBuilder.buildFuture();
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
		return List.of("0", "10", "@", "@[uuid=586ca6a2-ad52-4b4e-8e95-2222ae39cb7a]", "@[id={score=scoreboard_name}]", "@[type=basic]");
	}
}
