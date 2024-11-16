package tfc.dynamicportals.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.function.Predicate;

public class DataHolderNode<T> extends LiteralCommandNode<T> {
    protected int len;
    protected DypoContextBuilder dctx;

    public DataHolderNode(
            String literal, Command<T> command,
            Predicate<T> requirement, CommandNode<T> redirect,
            RedirectModifier<T> modifier, boolean forks
    ) {
        super(literal, command, requirement, redirect, modifier, forks);
    }
}
