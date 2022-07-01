package tfc.dynamicportals.command.args;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.command.CommandPortal;
import tfc.dynamicportals.command.FullPortalFilter;
import tfc.dynamicportals.command.PortalFilter;

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
	
	public FullPortalFilter parse(StringReader p_120843_) throws CommandSyntaxException {
		// TODO: check
		if (p_120843_.canRead()) {
//			String str = p_120843_.readString();
			String str = "";
			while (p_120843_.canRead()) {
				if (p_120843_.peek() == ' ') break;
				str += p_120843_.read();
			}
			try {
				int id = Integer.parseInt(str);
				return (PortalFilter) portals -> {
					for (CommandPortal portal : portals)
						if (portal.myId() == id)
							return new CommandPortal[]{portal};
					return null;
				};
			} catch (Throwable ignored) {
			}
			if (str.equals("@")) return (PortalFilter) (portals) -> portals.toArray(new CommandPortal[0]);
			// TODO: actual selectors
			if (str.startsWith("@[") && str.endsWith("]")) {
				str = str.substring(2, str.length() - 1);
				String[] args = str.split(",");
				int cnt = 0;
				for (char c : str.toCharArray()) {
					if (c == ',') cnt++;
				}
				FullPortalFilter filter = (portals, ctx) -> portals.toArray(new CommandPortal[0]);
				boolean success = false;
				if (args.length == (cnt + 1)) {
					for (String arg : args) {
						String[] split = arg.split("=", 2);
						if (split.length != 2) {
							throw new CommandSyntaxException(null, new LiteralMessage("Invalid Selector, all equal signs should have text to both the left and right."));
						}
						
						success = true;
						
						if (split[0].equals("type")) {
							FullPortalFilter temp = filter;
							filter = (portals, ctx) -> {
								ArrayList<CommandPortal> output = new ArrayList<>();
								for (CommandPortal portal : temp.filter(portals, ctx)) {
									if (portal.type().equals(split[1])) {
										output.add(portal);
									}
								}
								return output.toArray(new CommandPortal[0]);
							};
						} else if (split[0].equals("id")) {
							FullPortalFilter temp = filter;
							int i = Integer.parseInt(split[1]);
							filter = (portals, ctx) -> {
								ArrayList<CommandPortal> output = new ArrayList<>();
								for (CommandPortal portal : temp.filter(portals, ctx)) {
									if (portal.myId() == i) {
										output.add(portal);
									}
								}
								return output.toArray(new CommandPortal[0]);
							};
						} else if (split[0].equals("uuid")) {
							FullPortalFilter temp = filter;
							filter = (portals, ctx) -> {
								ArrayList<CommandPortal> output = new ArrayList<>();
								for (CommandPortal portal : temp.filter(portals, ctx)) {
									System.out.println(((AbstractPortal) portal).getUUID());
									if (((AbstractPortal) portal).getUUID().toString().equals(split[1])) {
										output.add(portal);
									}
								}
								return output.toArray(new CommandPortal[0]);
							};
						}
					}
				}
				if (success) return filter;
				// TODO: translation
				throw new CommandSyntaxException(null, new LiteralMessage("Invalid Selector, selector likely ends with a comma that shouldn't be there."));
			}
			throw new CommandSyntaxException(null, new LiteralMessage("Invalid Selector, unknown reason"));
		}
		throw new CommandSyntaxException(null, new LiteralMessage("Invalid Selector, likely empty?"));
	}
	
	// TODO: some clean up here would be nice
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
		if (!(pContext.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			String rem = pBuilder.getRemaining();
			if (rem.equals("@")) {
				return SharedSuggestionProvider.suggest(new String[]{"@["}, pBuilder);
			} else if (rem.startsWith("@[")) {
				ArrayList<String> suggestions = new ArrayList<>();
				if (rem.lastIndexOf(",") < rem.lastIndexOf("=")) {
					int max = Math.max(rem.indexOf("["), rem.lastIndexOf(","));
					String sub = rem.substring(max + 1);
					if (sub.startsWith("type=")) {
						String[] options = new String[]{
								"basic",
								"nether",
								"end",
						};
						SuggestionsBuilder builder = new SuggestionsBuilder(pBuilder.getInput(), pBuilder.getStart() + rem.lastIndexOf("=") + 1);
						String substr = rem.substring(rem.lastIndexOf("=") + 1);
						for (String option : options) {
							if (option.startsWith(substr)) {
								builder.suggest(option);
							}
						}
						pBuilder.add(builder);
					}
				} else {
					if (rem.endsWith(",") || rem.endsWith("[")) {
						String[] options = new String[]{
								"uuid",
								"id",
								"type",
						};
						SuggestionsBuilder builder = new SuggestionsBuilder(pBuilder.getInput(), pBuilder.getStart() + rem.length());
						for (String option : options)
							builder.suggest(option);
						pBuilder.add(builder);
					} else {
						// TODO: validation
						String[] options = new String[]{
								"uuid",
								"id",
								"type",
						};
						int max = Math.max(rem.indexOf("["), rem.lastIndexOf(","));
						SuggestionsBuilder builder = new SuggestionsBuilder(pBuilder.getInput(), pBuilder.getStart() + max + 1);
						String substr = pBuilder.getRemaining().substring(max + 1);
						System.out.println(substr);
						for (String option : options) {
							if (option.startsWith(substr) || option.equals(substr)) {
								builder.suggest(option);
							}
						}
						pBuilder.add(builder);
						suggestions.add(rem + "]");
					}
				}
//				return SharedSuggestionProvider.suggest(suggestions.toArray(new String[0]), pBuilder);
				return pBuilder.buildFuture();
			}
			// TODO: suggestion provider for portal selectors
			return SharedSuggestionProvider.suggest(new String[0], pBuilder);
		}
	}
	
	public Collection<String> getExamples() {
		return List.of("0", "10", "@", "@[uuid=586ca6a2-ad52-4b4e-8e95-2222ae39cb7a]", "@[id={score=scoreboard_name}]", "@[type=basic]");
	}
}
