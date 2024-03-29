package tfc.dynamicportals.util;

import java.util.*;

public class ReadOnlyList<T> implements List<T> {
    List<T> internal;

    public ReadOnlyList(List<T> internal) {
        this.internal = internal;
    }

    @Override
    public int size() {
        return internal.size();
    }

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return internal.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return internal.iterator();
    }

    @Override
    public Object[] toArray() {
        return internal.toArray();
    }

    
    @Override
    public <T1> T1[] toArray(T1 [] a) {
        return internal.toArray(a);
    }

    @Override
    public boolean add(T t) {
        throw new RuntimeException("Unsupported operation: add");
    }

    @Override
    public boolean remove(Object o) {
        throw new RuntimeException("Unsupported operation: remove");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return new HashSet<>(internal).containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new RuntimeException("Unsupported operation: addAll");
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new RuntimeException("Unsupported operation: addAll");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new RuntimeException("Unsupported operation: removeAll");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new RuntimeException("Unsupported operation: retainAll");
    }

    @Override
    public void clear() {
        throw new RuntimeException("Unsupported operation: clear");
    }

    @Override
    public T get(int index) {
        return internal.get(index);
    }

    @Override
    public T set(int index, T element) {
        throw new RuntimeException("Unsupported operation: set");
    }

    @Override
    public void add(int index, T element) {
        throw new RuntimeException("Unsupported operation: add");
    }

    @Override
    public T remove(int index) {
        throw new RuntimeException("Unsupported operation: remove");
    }

    @Override
    public int indexOf(Object o) {
        return internal.indexOf(o);
    }
    
    @Override
    public int lastIndexOf(Object o) {
        return internal.lastIndexOf(o);
    }
    
    @Override
    public ListIterator<T> listIterator() {
        return internal.listIterator();
    }
    
    @Override
    public ListIterator<T> listIterator(int index) {
        return internal.listIterator(index);
    }
    
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return new ReadOnlyList<>(internal.subList(fromIndex, toIndex));
    }
}
