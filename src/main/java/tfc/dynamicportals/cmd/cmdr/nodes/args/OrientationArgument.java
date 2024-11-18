package tfc.dynamicportals.cmd.cmdr.nodes.args;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import tfc.dynamicportals.cmd.cmdr.CmdRContext;
import tfc.dynamicportals.cmd.cmdr.exception.DypoException;
import tfc.dynamicportals.cmd.cmdr.nodes.CmdRNode;
import tfc.dynamicportals.cmd.cmdr.nodes.util.RelAbsParser;

import java.util.concurrent.CompletableFuture;

public class OrientationArgument<T, A, B> extends CmdRNode<T, A, B> {
    boolean acceptQuaternion = true;
    boolean acceptPitchYaw = true;
    boolean acceptEuler = true;
    boolean multiAccept = true;

    public OrientationArgument() {
    }

    public OrientationArgument<T, A, B> setAcceptQuaternion(boolean acceptQuaternion) {
        this.acceptQuaternion = acceptQuaternion;
        multiAccept = acceptPitchYaw || acceptEuler;
        return this;
    }

    public OrientationArgument<T, A, B> setAcceptPitchYaw(boolean acceptPitchYaw) {
        this.acceptPitchYaw = acceptPitchYaw;
        multiAccept = acceptQuaternion || acceptEuler;
        return this;
    }

    public OrientationArgument<T, A, B> setAcceptEuler(boolean acceptEuler) {
        this.acceptEuler = acceptEuler;
        multiAccept = acceptQuaternion || acceptPitchYaw;
        return this;
    }

    protected char acceptMode(StringReader reader) throws DypoException {
        if (!multiAccept) {
            if (acceptQuaternion) return 'q';
            if (acceptEuler) return 'e';
            if (acceptPitchYaw) return 'p';
            throw new RuntimeException("Some developer unset all valid selection modes.");
        }

        if (!reader.canRead()) {
            throw new DypoException("Expecting rotation mode", reader, reader.getCursor());
        }
        char chr = reader.read();
        switch (chr) {
            case 'q' -> {
                if (!acceptQuaternion) throw new DypoException("Invalid rotation mode", reader, reader.getCursor() - 1);
                return 'q';
            }
            case 'e' -> {
                if (!acceptEuler) throw new DypoException("Invalid rotation mode", reader, reader.getCursor() - 1);
                return 'e';
            }
            case 'p' -> {
                if (!acceptPitchYaw) throw new DypoException("Invalid rotation mode", reader, reader.getCursor() - 1);
                return 'p';
            }
            default -> throw new DypoException("Invalid rotation mode", reader, reader.getCursor() - 1);
        }
    }

    @Override
    public CommandSyntaxException parse(StringReader reader, CommandContextBuilder<T> builder, CmdRContext dpbuilder) {
        try {
            int cursor = reader.getCursor();
            char mode = acceptMode(reader);
            builder.withArgument(
                    "__placeholder_argument__" + builder.getArguments().size(),
                    new ParsedArgument<>(cursor, reader.getCursor(), null)
            );
            reader.skipWhitespace();

            cursor = reader.getCursor();

            RelAbsParser.RelAbsData arg0 = RelAbsParser.parse(reader);
            reader.skipWhitespace();
            RelAbsParser.RelAbsData arg1 = RelAbsParser.parse(reader);
            RelAbsParser.RelAbsData arg2 = null;
            if (mode == 'e' || mode == 'q') {
                reader.skipWhitespace();
                arg2 = RelAbsParser.parse(reader);
            }
            RelAbsParser.RelAbsData arg3 = null;
            if (mode == 'q') {
                reader.skipWhitespace();
                arg3 = RelAbsParser.parse(reader);
            }

            builder.withArgument(
                    "__placeholder_argument__" + builder.getArguments().size(),
                    new ParsedArgument<>(cursor, reader.getCursor(), null)
            );

            dpbuilder.setExecData(switch (mode) {
                case 'p' -> throw new DypoException("NYI");
                case 'e' -> throw new DypoException("NYI");
                case 'q' -> new OrientationData.QuatData(arg0, arg1, arg2, arg3);
                default -> throw new DypoException("what.", reader);
            });
        } catch (CommandSyntaxException err) {
            return err;
        }
        return null;
    }

    @Override
    public boolean isValidInput(String input) {
        return false;
    }

    @Override
    public CompletableFuture<Suggestions> mySuggestions(CommandContext<T> context, SuggestionsBuilder builder, CmdRContext ctx) {
        StringReader reader = new StringReader(builder.getRemaining());
        char mode = acceptQuaternion ? 'q' : (acceptEuler ? 'e' : 'p');
        boolean chessBattleAdvanced = false;
        if (multiAccept &&
                Character.isWhitespace(
                        builder.getInput().charAt(builder.getStart() - 1)
                ) &&
                builder.getRemaining().isEmpty()
        ) {
            if (acceptQuaternion) builder.suggest("q", new LiteralMessage("quaternion"));
            if (acceptEuler) builder.suggest("e", new LiteralMessage("euler"));
            if (acceptPitchYaw) builder.suggest("p", new LiteralMessage("pitch/yaw"));
        } else if (multiAccept) {
            mode = reader.read();
            int ocur = reader.getCursor();
            reader.skipWhitespace();
            chessBattleAdvanced = reader.getCursor() != ocur;
        } else chessBattleAdvanced = true;

        int argC = switch (mode) {
            case 'q' -> 4;
            case 'e' -> 3;
            case 'p' -> 2;
            default -> 0;
        };

        if (chessBattleAdvanced) {
            String existent = "";
            for (int i = 0; i < argC; i++) {
                if (!reader.canRead()) {
                    builder.suggest(existent + "~");
                    existent += "~";
                }
                while (reader.canRead()) {
                    char c = reader.read();
                    if (!Character.isWhitespace(c)) {
                        existent += c;
                    } else break;
                }
                existent += " ";
            }
        }

        return CompletableFuture.completedFuture(builder.build());
    }
}
