package tfc.dynamicportals.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.SingleRedirectModifier;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public class DypoBuilder<S> extends LiteralArgumentBuilder<S> {
    DypoCmdNode<S> node;

    public DypoBuilder(DypoCmdNode<S> node) {
        super(node.getName());
        this.node = node;
    }

    @Override
    protected DypoBuilder<S> getThis() {
        return this;
    }

    @Override
    public LiteralCommandNode<S> build() {
        return node;
    }

    @Override
    public DypoBuilder<S> then(ArgumentBuilder<S, ?> argument) {
        return this;
    }

    @Override
    public DypoBuilder<S> then(CommandNode<S> argument) {
        return this;
    }

    @Override
    public Collection<CommandNode<S>> getArguments() {
        return Collections.singleton(node);
    }

    @Override
    public DypoBuilder<S> executes(Command<S> command) {
        return this;
    }

    @Override
    public Command<S> getCommand() {
        return node.getCommand();
    }

    @Override
    public DypoBuilder<S> requires(Predicate<S> requirement) {
        return this;
    }

    @Override
    public Predicate<S> getRequirement() {
        return node.getRequirement();
    }

    @Override
    public DypoBuilder<S> redirect(CommandNode<S> target) {
        return this;
    }

    @Override
    public DypoBuilder<S> redirect(CommandNode<S> target, SingleRedirectModifier<S> modifier) {
        return this;
    }

    @Override
    public DypoBuilder<S> fork(CommandNode<S> target, RedirectModifier<S> modifier) {
        return this;
    }

    @Override
    public DypoBuilder<S> forward(CommandNode<S> target, RedirectModifier<S> modifier, boolean fork) {
        return this;
    }

    @Override
    public CommandNode<S> getRedirect() {
        return super.getRedirect();
    }

    @Override
    public RedirectModifier<S> getRedirectModifier() {
        return super.getRedirectModifier();
    }

    @Override
    public boolean isFork() {
        return node.isFork();
    }
}
