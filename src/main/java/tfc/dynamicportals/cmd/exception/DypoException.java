package tfc.dynamicportals.cmd.exception;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class DypoException extends CommandSyntaxException {
    public DypoException(
            CommandExceptionType type,
            String message,
            StringReader reader
    ) {
        super(
                type, new LiteralMessage(message),
                reader.getString(), reader.getCursor()
        );
    }

    public DypoException(
            String message,
            StringReader reader
    ) {
        super(
                DypoExceptionType.INSTANCE, new LiteralMessage(message),
                reader.getString(), reader.getCursor()
        );
    }

    public DypoException(String message) {
        super(
                DypoExceptionType.INSTANCE, new LiteralMessage(message)
        );
    }
}
