package tfc.dynamicportals.cmd.cmdr.nodes.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import tfc.dynamicportals.cmd.cmdr.exception.DypoException;

public class RelAbsParser {
    public record RelAbsData(boolean isRel, double data) {
        public double get(double i) {
            if (isRel) return i + data;
            return data;
        }

        public float get(float i) {
            if (isRel) return i + (float) data;
            return (float) data;
        }
    }

    public static RelAbsData parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead())
            throw new DypoException("Incorrect argument for command", reader, reader.getCursor());

        char first = reader.peek();
        boolean isRel = first == '~';
        if (isRel) reader.skip();

        char chr = ' ';
        if (reader.canRead())
            chr = reader.peek();
        double dbl = 0.0;
        if (isRel && !reader.canRead()) {
            dbl = 0.0;
        } else if (!isRel || !Character.isWhitespace(chr)) {
            try {
                dbl = reader.readDouble();
            } catch (CommandSyntaxException exception) {
                throw new DypoException(
                        "Invalid character " + reader.peek(),
                        reader
                );
            }
        }

        return new RelAbsData(isRel, dbl);
    }
}
