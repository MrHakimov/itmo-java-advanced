package ru.ifmo.rain.hakimov.arrayset;

import java.util.*;

@SuppressWarnings("unused")
public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private static class ReversibleArrayList<T> extends AbstractList<T> implements RandomAccess {
        List<T> data;
        boolean reversed;

        ReversibleArrayList(Collection<T> collection) {
            this.data = List.copyOf(collection);
            this.reversed = false;
        }

        ReversibleArrayList(List<T> list) {
            this.data = Collections.unmodifiableList(list);
            this.reversed = false;
        }

        ReversibleArrayList(ReversibleArrayList<T> reversedArray, boolean reversed) {
            this.data = reversedArray.data;
            this.reversed = reversedArray.reversed ^ reversed;
        }

        private int reversedIndex(int index) {
            return size() - index - 1;
        }

        @Override
        public ReversibleArrayList<T> subList(int fromIndex, int toIndex) {
            if (reversed) {
                return new ReversibleArrayList<>(data.subList(reversedIndex(toIndex - 1),
                        reversedIndex(fromIndex) + 1));
            } else {
                return new ReversibleArrayList<>(data.subList(fromIndex, toIndex));
            }
        }

        public void reverse() {
            reversed ^= true;
        }

        @Override
        public T get(int index) {
            return data.get(reversed ? reversedIndex(index) : index);
        }

        @Override
        public int size() {
            return data.size();
        }
    }

    private final ReversibleArrayList<E> data;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }


    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;

        TreeSet<E> tmp = new TreeSet<>(comparator);
        tmp.addAll(collection);

        data = new ReversibleArrayList<>(tmp);
    }

    private boolean isValid(int index) {
        return (0 <= index && index < size());
    }

    private E getNullable(int index) {
        return isValid(index) ? data.get(index) : null;
    }

    private int binarySearch(E o) {
        return Collections.binarySearch(data, (E) o, this::compare);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return binarySearch((E) o) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return data.iterator();
    }

    @SuppressWarnings("unchecked")
    private int compare(E e1, E e2) {
        return (comparator == null) ? ((Comparable<E>) e1).compareTo(e2) : comparator.compare(e1, e2);
    }

    private int getIndex(E element, boolean inclusive, boolean lower) {
        int index = binarySearch(element);

        if (index >= 0) {
            return index + (inclusive ? 0 : (lower ? -1 : 1));
        } else {
            return (-index - 1) + (lower ? -1 : 0);
        }
    }

    @Override
    public E lower(E element) {
        return getNullable(getIndex(element, false, true));
    }

    @Override
    public E floor(E element) {
        return getNullable(getIndex(element, true, true));
    }

    @Override
    public E ceiling(E element) {
        return getNullable(getIndex(element, true, false));
    }

    @Override
    public E higher(E element) {
        return getNullable(getIndex(element, false, false));
    }

    private final String UNSUPPORTED_OPERATION_MESSAGE = "ArraySet is immutable, operation can't be performed: ";

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE + "pollFirst");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE + "pollLast");
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversibleArrayList<>(data, true), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException(fromElement + " should be less than " + toElement + "!");
        }
        return subSetImplementation(fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        } else {
            return subSetImplementation(first(), true, toElement, inclusive);
        }
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        } else {
            return subSetImplementation(fromElement, inclusive, last(), true);
        }
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    private final String NO_SUCH_ELEMENT_MESSAGE = "ArraySet is empty! Can't get ";

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException(NO_SUCH_ELEMENT_MESSAGE + "first element.");
        } else {
            return data.get(0);
        }
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException(NO_SUCH_ELEMENT_MESSAGE + "last element.");
        } else {
            return data.get(size() - 1);
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE + "addAll");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE + "retainAll");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE + "clear");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE + "remove");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE + "removeAll");
    }

    private NavigableSet<E> subSetImplementation(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int fromIndex = getIndex(fromElement, fromInclusive, false);
        int toIndex = getIndex(toElement, toInclusive, true);

        return fromIndex > toIndex ? new ArraySet<>(comparator) : new ArraySet<>(data.subList(fromIndex, toIndex + 1), comparator);
    }

    private ArraySet(ReversibleArrayList<E> reversibleArray, Comparator<? super E> comparator) {
        this.data = reversibleArray;
        this.comparator = comparator;
    }

    @Override
    public int size() {
        return data.size();
    }
}

