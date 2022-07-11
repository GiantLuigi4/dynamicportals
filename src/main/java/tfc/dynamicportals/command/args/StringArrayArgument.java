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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class StringArrayArgument implements ArgumentType<String> {
	private final String[] options;
	
	public StringArrayArgument(String[] options) {
		this.options = options;
	}
	
	public static StringArrayArgument of(String[] possibilities) {
		return new StringArrayArgument(possibilities);
	}
	
	public String parse(StringReader reader) throws CommandSyntaxException {
		if (reader.canRead()) {
			String parsed = reader.readString();
			for (String option : options) {
				if (parsed.equalsIgnoreCase(option)) return option;
			}
			throw new SimpleCommandExceptionType(new TranslatableComponent("argument.entity.options.unknown", parsed).append("; Option must be one of: " + Arrays.toString(options))).createWithContext(reader);
		}
		throw new SimpleCommandExceptionType(new TranslatableComponent("dynamicportals.command.cheese.unknown")).createWithContext(reader);
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
		if (pContext.getSource() instanceof SharedSuggestionProvider)
			return SharedSuggestionProvider.suggest(options, pBuilder);
		else return Suggestions.empty();
	}
	
	public Collection<String> getExamples() {
		return java.util.List.of(options);
	}
}
