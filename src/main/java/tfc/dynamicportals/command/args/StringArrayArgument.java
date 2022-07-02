package tfc.dynamicportals.command.args;

import com.mojang.brigadier.LiteralMessage;
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
	public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos3d.incomplete"));
	public static final SimpleCommandExceptionType ERROR_MIXED_TYPE = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos.mixed"));
	
	private final String[] options;
	
	public StringArrayArgument(String[] options) {
		this.options = options;
	}
	
	public static StringArrayArgument of(String[] possibilities) {
		return new StringArrayArgument(possibilities);
	}
	
	public String parse(StringReader p_120843_) throws CommandSyntaxException {
		// TODO: check
		if (p_120843_.canRead()) {
			String str = p_120843_.readString();
			for (String option : options) {
				if (str.equals(option)) return option;
			}
			throw new CommandSyntaxException(null, new LiteralMessage("Input stream must be one of: " + Arrays.toString(options)));
		}
		throw new CommandSyntaxException(null, new LiteralMessage("unknown error"));
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
		if (!(pContext.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			return SharedSuggestionProvider.suggest(options, pBuilder);
		}
	}
	
	public Collection<String> getExamples() {
		return java.util.List.of(options);
	}
}
