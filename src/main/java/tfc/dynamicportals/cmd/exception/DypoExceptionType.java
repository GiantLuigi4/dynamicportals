package tfc.dynamicportals.cmd.exception;

import com.mojang.brigadier.exceptions.CommandExceptionType;

public class DypoExceptionType implements CommandExceptionType {
    public static final CommandExceptionType INSTANCE = new DypoExceptionType();

    public DypoExceptionType() {
    }
}
