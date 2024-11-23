package tfc.dynamicportals.network.util.optim;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ReferenceMap<K, V> implements Map<K, V> {
    Map<K, Object[]> back;

    public ReferenceMap(Map<K, Object[]> back) {
        this.back = back;
    }

    @Override
    public final int size() {
        return back.size();
    }

    @Override
    public final boolean isEmpty() {
        return back.isEmpty();
    }

    @Override
    public final boolean containsKey(Object key) {
        return back.containsKey(key);
    }

    @Override
    public final boolean containsValue(Object value) {
        return back.containsValue(new Object[]{value});
    }

    @Override
    public final V get(Object key) {
        Object[] o = back.get(key);
        if (o == null) return null;
        return (V) o[0];
    }

    @Nullable
    @Override
    public final V put(K key, V value) {
        Object[] o = back.get(key);
        // compute if absent has meaningful overhead
        //noinspection Java8MapApi
        if (o == null)
            back.put(key, o = new Object[1]);
        Object prev = o[0];
        o[0] = value;
        return (V) prev;
    }

    @Override
    public final V remove(Object key) {
        Object[] o = back.remove(key);
        if (o == null) return null;
        return (V) o[0];
    }

    @Override
    public final void putAll(@NotNull Map<? extends K, ? extends V> m) {
        throw new RuntimeException("TODO");
    }

    @Override
    public final void clear() {
        back.clear();
    }

    @NotNull
    @Override
    public final Set<K> keySet() {
        return back.keySet();
    }

    @NotNull
    @Override
    public final Collection<V> values() {
        throw new RuntimeException("TODO");
    }

    @NotNull
    @Override
    public final Set<Entry<K, V>> entrySet() {
        throw new RuntimeException("TODO");
    }

    @Override
    public final V getOrDefault(Object key, V defaultValue) {
        return (V) back.getOrDefault(key, new Object[]{defaultValue})[0];
    }

    @Override
    public final void forEach(BiConsumer<? super K, ? super V> action) {
        back.forEach((k, v) -> action.accept(k, (V) v[0]));
    }

    @Override
    public final void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new RuntimeException("TODO");
    }

    @Nullable
    @Override
    public final V putIfAbsent(K key, V value) {
        return (V) back.putIfAbsent(key, new Object[]{value})[0];
    }

    @Override
    public final boolean remove(Object key, Object value) {
        return back.remove(key, new Object[]{value});
    }

    @Override
    public final boolean replace(K key, V oldValue, V newValue) {
        Object[] o = back.get(key);
        if (o == null) return false;
        Object prev = o[0];
        if (prev.equals(oldValue)) {
            o[0] = newValue;
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public final V replace(K key, V value) {
        Object[] o = back.get(key);
        if (o == null)
            return null;
        Object prev = o[0];
        o[0] = value;
        return (V) prev;
    }

    @Override
    public final V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        throw new RuntimeException("TODO");
    }

    @Override
    public final V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new RuntimeException("TODO");
    }

    @Override
    public final V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new RuntimeException("TODO");
    }

    @Override
    public final V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new RuntimeException("TODO");
    }
}
