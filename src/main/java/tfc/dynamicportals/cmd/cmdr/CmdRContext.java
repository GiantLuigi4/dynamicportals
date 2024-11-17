package tfc.dynamicportals.cmd.cmdr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.dynamicportals.cmd.cmdr.nodes.CmdRNode;

import java.util.HashMap;
import java.util.Stack;

public class CmdRContext {
    @NotNull CmdRNode lastNode;
    CmdRContext parent;
    int suggestionOffset;
    Stack<CmdRNode> nodes = new Stack<>();
    Stack<Integer> ends = new Stack<>();
    Stack<Object> dataPoints = new Stack<>();
    Object dataPoint;
    int dummyPoint = 0;

    public CmdRContext(@NotNull CmdRNode lastNode, CmdRContext parent) {
        this.lastNode = lastNode;
        this.parent = parent;
        this.data = new HashMap<>(parent.data);
        dummyPoint = parent.dummyPoint + 1;
    }

    public CmdRNode getLastNode() {
        return lastNode;
    }

    public CmdRContext(@NotNull CmdRNode lastNode) {
        this.lastNode = lastNode;
        parent = null;
        this.data = new HashMap<>();
    }

    HashMap<CmdRNode<?, ?, ?>, Object> data;

    public @Nullable <T, V> T getData(CmdRNode<V, ?, ?> node) {
        return (T) data.get(node);
    }

    public <T, V> void setData(CmdRNode<T, ?, ?> node, @NotNull V data) {
        if (data == null) throw new RuntimeException("Data provided is null");
        this.data.put(node, data);
    }

    public <T> void setExecData(T data) {
        this.dataPoint = data;
    }

    public void set(int cursor, CmdRContext dctx) {
        for (Object dataPoint : dctx.dataPoints)
            dataPoints.push(dataPoint);
        dataPoints.push(dctx.dataPoint);

        nodes.push(lastNode);
        ends.push(cursor);

        for (CmdRNode node : dctx.nodes)
            nodes.push(node);
        for (Integer end : dctx.ends)
            ends.push(end);

        this.lastNode = dctx.lastNode;
        data.clear();
        data.putAll(dctx.data);
    }

    public void finish(int cursor) {
        dataPoints.push(dataPoint);
        nodes.push(lastNode);
        ends.push(cursor);
    }

    public int getSuggestionOffset() {
        return suggestionOffset;
    }

    public <T> T getExecData() {
        return (T) dataPoints.peek();
    }

    void progress() {
        dataPoints.pop();
    }
}
