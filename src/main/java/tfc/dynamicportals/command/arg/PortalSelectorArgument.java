package tfc.dynamicportals.command.arg;

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
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.command.PortalNetFilter;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.arguments.selector.EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE;
import static net.minecraft.commands.arguments.selector.options.EntitySelectorOptions.ERROR_UNKNOWN_OPTION;

public class PortalSelectorArgument implements ArgumentType<PortalNetFilter> {
	public List<String> portalTypes = List.of("basic", "nether", "end", "mirror");
	public List<String> selectorOptions = List.of("id", "type");
	
	public PortalSelectorArgument() {
	}
	
	public static PortalSelectorArgument create() {
		return new PortalSelectorArgument();
	}
	
	@Override
	public PortalNetFilter parse(StringReader reader) throws CommandSyntaxException {
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
							PortalNetFilter filter = (net, ctx) -> net.getPortals().toArray(new AbstractPortal[0]);
							for (String[] split : selectors) {
								PortalNetFilter oldFilter = filter;
								filter = (net, ctx) -> {
									ArrayList<AbstractPortal> output = new ArrayList<>();
									for (AbstractPortal portal : oldFilter.apply(net, ctx)) {
										switch (split[0]) {
											case "type" -> {
												if (portal.type.getRegistryName().getPath().equals(split[1])) output.add(portal);
											}
											case "id" -> {
												if (Integer.parseInt(split[1]) < net.getPortals().size()) output.add(net.getPortals().get(Integer.parseInt(split[1])));
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
						return (net, context) -> net.getPortals().toArray(new AbstractPortal[0]);
					} else {
						throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(reader);
					}
				} else {
					return (net, context) -> net.getPortals().toArray(new AbstractPortal[0]);
				}
			}
		} else {
			throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(reader);
		}
		return ((portals, ctx) -> null);
	}
	
	public String[] parseSingleFilter(StringReader reader) throws CommandSyntaxException {
		String option = reader.readString();
		if (option.isEmpty()) throw new SimpleCommandExceptionType(Component.translatable("dynamicportals.command.cheese.empty_option")).createWithContext(reader);
		else if (!selectorOptions.contains(option)) throw ERROR_UNKNOWN_OPTION.createWithContext(reader, option);
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
						options.addAll(portalTypes);
					} else if (selectorType.startsWith("id=")) {
						//TODO: uhm idk if I should reference the freaking Minecraft class lmao
						PortalNet net = ((NetworkHolder) Minecraft.getInstance().level).getPortalNetworks().stream().filter((n) -> n.getUUID().toString().equals(context.getArgument("portal_net", String.class))).findFirst().get();
						for (int i = 0; i < net.getPortals().size(); i++) options.add(Integer.toString(i));
					}
				} else {
					for (String o : this.selectorOptions)
						options.add(o + "=");
				}
				
				for (String option : options) {
					if (option.equals(argument)) {
						return SharedSuggestionProvider.suggest(new String[]{",", "]"}, builder.createOffset(builder.getStart() + builder.getRemaining().length()));
					}
				}
				
				return SharedSuggestionProvider.suggest(options, builder.createOffset(builder.getStart() + (selectorEnd < 0 ? selectorStart : selectorEnd) + 1));
			}
			return SharedSuggestionProvider.suggest(List.of("@"), builder);
		} else {
			return Suggestions.empty();
		}
	}
	
	@Override
	public Collection<String> getExamples() {
		return List.of("@", "@[uuid=586ca6a2-ad52-4b4e-8e95-2222ae39cb7a]", "@[type=basic]");
	}
}
