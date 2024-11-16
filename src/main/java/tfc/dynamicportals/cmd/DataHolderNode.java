package tfc.dynamicportals.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public class DataHolderNode<T> extends LiteralCommandNode<T> {
    protected int len;
    protected DypoContextBuilder dctx;
    DypoCmdNode parent;

    public DataHolderNode(
            String literal, Command<T> command,
            Predicate<T> requirement, CommandNode<T> redirect,
            RedirectModifier<T> modifier, boolean forks,
            DypoCmdNode parent
    ) {
        super(literal, command, requirement, redirect, modifier, forks);
        this.parent = parent;
    }

    @Override
    public Collection<CommandNode<T>> getChildren() {
        return Collections.singleton(parent);
    }

    @Override
    public CommandNode<T> getChild(String name) {
        if (parent.name.equals(name)) return parent;
        return super.getChild(name);
    }
}
