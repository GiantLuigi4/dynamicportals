package tfc.dynamicportals.cmd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.dynamicportals.cmd.nodes.DypoNode;

import java.util.HashMap;
import java.util.Stack;

public class DypoContextBuilder {
    @NotNull DypoNode lastNode;
    DypoContextBuilder parent;
    int suggestionOffset;
    Stack<DypoNode> nodes = new Stack<>();
    Stack<Integer> ends = new Stack<>();

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

    HashMap<DypoNode<?, ?, ?>, Object> data;

    public @Nullable <T, V> T getData(DypoNode<V, ?, ?> node) {
        return (T) data.get(node);
    }

    public <T, V> void setData(DypoNode<T, ?, ?> node, @NotNull V data) {
        if (data == null) throw new RuntimeException("Data provided is null");
        this.data.put(node, data);
    }

    public void set(int cursor, DypoContextBuilder dctx) {
        nodes.push(lastNode);
        ends.push(cursor);

        for (DypoNode node : dctx.nodes)
            nodes.push(node);
        for (Integer end : dctx.ends)
            ends.push(end);

        this.lastNode = dctx.lastNode;
        data.clear();
        data.putAll(dctx.data);
    }

    public void finish(int cursor) {
        nodes.push(lastNode);
        ends.push(cursor);
    }

    public int getSuggestionOffset() {
        return suggestionOffset;
    }
}
