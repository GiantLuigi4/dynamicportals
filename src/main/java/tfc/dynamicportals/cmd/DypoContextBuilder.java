package tfc.dynamicportals.cmd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.dynamicportals.cmd.nodes.DypoNode;

import java.util.HashMap;

public class DypoContextBuilder {
    @NotNull DypoNode lastNode;
    boolean propagate = false;
    DypoContextBuilder parent;
    int suggestionOffset;

    public DypoContextBuilder(@NotNull DypoNode lastNode, DypoContextBuilder parent) {
        this.lastNode = lastNode;
        this.parent = parent;
        this.data = new HashMap<>(parent.data);
    }

    public DypoNode getLastNode() {
        return lastNode;
    }

    public DypoContextBuilder(@NotNull DypoNode lastNode) {
        this.lastNode = lastNode;
        parent = null;
        this.data = new HashMap<>();
    }

    HashMap<DypoNode<?>, Object> data;

    public @Nullable <T, V> T getData(DypoNode<V> node) {
        return (T) data.get(node);
    }

    public <T, V> void setData(DypoNode<T> node, @NotNull V data) {
        if (data == null) throw new RuntimeException("Data provided is null");
        this.data.put(node, data);
    }

    public void set(DypoContextBuilder dctx) {
        this.lastNode = dctx.lastNode;
        data.clear();
        data.putAll(dctx.data);
    }

    public void setPropagate() {
        propagate = true;
    }

    public void propagate(DypoContextBuilder ctx) {
        if (ctx.propagate) {
            this.lastNode = ctx.lastNode;
        }
    }

    public int getSuggestionOffset() {
        return suggestionOffset;
    }

    public boolean isPropagate() {
        return propagate;
    }
}
