package tfc.dynamicportals.cmd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.dynamicportals.cmd.nodes.DypoNode;

import java.util.HashMap;

public class DypoContextBuilder {
    DypoContextBuilder parent;

    public DypoContextBuilder(DypoContextBuilder parent) {
        this.parent = parent;
        this.data = new HashMap<>(parent.data);
    }

    public DypoContextBuilder() {
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
        data.clear();
        data.putAll(dctx.data);
    }
}
