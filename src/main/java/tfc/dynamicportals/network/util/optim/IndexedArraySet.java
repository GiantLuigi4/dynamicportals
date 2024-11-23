package tfc.dynamicportals.network.util.optim;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class IndexedArraySet<K> implements Set<K> {
    final Object[] vals;
    int elements = 0;
    final int len;
    final Set<K> fallback = new HashSet<>();

    public IndexedArraySet(int size) {
        vals = new Object[size];
        this.len = size;
    }

    @Override
    public final int size() {
        return elements;
    }

    @Override
    public final boolean isEmpty() {
        return elements == 0;
    }

    @Override
    public final boolean contains(Object o) {
        int index = o.hashCode();
        if (index >= 0 && index < len)
            return vals[index] == o;
        return fallback.contains(o);
    }

    @NotNull
    @Override
    public final Iterator<K> iterator() {
        throw new RuntimeException();
    }

    @NotNull
    @Override
    public final Object[] toArray() {
        throw new RuntimeException();
    }

    @NotNull
    @Override
    public final <T> T[] toArray(@NotNull T[] a) {
        throw new RuntimeException();
    }

    @Override
    public final boolean add(K k) {
        int index = k.hashCode();
        if (index >= 0 && index < len) {
            Object o = vals[index];
            if (o == k) return false;
            vals[index] = k;
            elements++;
            return true;
        }
        return fallback.add(k);
    }

    @Override
    public final boolean remove(Object k) {
        int index = k.hashCode();
        if (index >= 0 && index < len) {
            Object o = vals[index];
            if (o != k) return false;
            vals[index] = null;
            elements--;
            return true;
        }
        return fallback.remove(k);
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c) {
        throw new RuntimeException();
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends K> c) {
        throw new RuntimeException();
    }

    @Override
    public final boolean retainAll(@NotNull Collection<?> c) {
        throw new RuntimeException();
    }

    @Override
    public final boolean removeAll(@NotNull Collection<?> c) {
        throw new RuntimeException();
    }

    @Override
    public final void clear() {
        Arrays.fill(vals, null);
        fallback.clear();
    }
}
