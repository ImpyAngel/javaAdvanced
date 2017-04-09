package ru.ifmo.ctddev.toropin.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E>
        implements NavigableSet<E> {

    final private List<E> sortedList;
    final private Comparator<E> comparator;

    public ArraySet() {
        sortedList = Collections.emptyList();
        comparator = null;
    }

    public ArraySet(Collection<E> collection, Comparator<E> comparator) {
        this(collection, comparator, false);
    }

    public ArraySet(Collection<? extends E> collection) {
        this((Collection<E>) collection, null);
    }

    private ArraySet(Collection<E> collection, Comparator<E> comparator, boolean isSorted) {
        this.comparator = comparator;
        if (!isSorted) {
            TreeSet<E> treeSet = new TreeSet<>(comparator);
            treeSet.addAll(collection);
            sortedList = new ArrayList<>(treeSet);
        } else {
            this.sortedList = (List<E>) collection;
        }
    }

    private boolean indexInBounds(int index) {
        return index >= 0 && index < sortedList.size();
    }

    private int indexFromBinarySearch(int ifEqual, int ifNeededLower, E e) {
        int result = Collections.binarySearch(sortedList, e, comparator);
        result = result >= 0
                ? result + ifEqual
                : -result - 1 - ifNeededLower;
        return result;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public E first() {
        if (sortedList.size() == 0) {
            throw new NoSuchElementException("List is empty");
        }
        return sortedList.get(0);
    }

    @Override
    public E last() {
        if (sortedList.size() == 0) {
            throw new NoSuchElementException("List is empty");
        }
        return sortedList.get(sortedList.size() - 1);
    }

    @Override
    public E lower(E e) {
        int result = indexFromBinarySearch(-1, 1, e);
        return indexInBounds(result) ? sortedList.get(result) : null;
    }

    @Override
    public E floor(E e) {
        int result = indexFromBinarySearch(0, 1, e);
        return indexInBounds(result) ? sortedList.get(result) : null;
    }

    @Override
    public E ceiling(E e) {
        int result = indexFromBinarySearch(0, 0, e);
        return indexInBounds(result) ? sortedList.get(result) : null;
    }

    @Override
    public E higher(E e) {
        int result = indexFromBinarySearch(1, 0, e);
        return indexInBounds(result) ? sortedList.get(result) : null;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                                  E toElement, boolean toInclusive) {

        int fromIndex = fromInclusive
                ? indexFromBinarySearch(0, 0, fromElement)
                : indexFromBinarySearch(1, 0, fromElement);

        int toIndex = toInclusive
                ? indexFromBinarySearch(0, 1, toElement) + 1
                : indexFromBinarySearch(-1, 1, toElement) + 1;

        if (fromIndex > toIndex) {
            fromIndex = toIndex;
        }

        return new ArraySet<>(sortedList.subList(fromIndex, toIndex), comparator, true);
    }

    @Override
    public NavigableSet<E> headSet(E e, boolean b) {
        return subSet(size() == 0 ? null : first(), true, e, b);
    }

    @Override
    public NavigableSet<E> tailSet(E e, boolean b) {
        return subSet(e, b, size() == 0 ? null : last(), true);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E e) {
        return headSet(e, false);
    }

    @Override
    public SortedSet<E> tailSet(E e) {
        return tailSet(e, true);
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(sortedList).iterator();
    }

    @Override
    public int size() {
        return sortedList == null ? 0 : sortedList.size();
    }

    private class DescendingList<T> extends AbstractList<T> implements RandomAccess {
        private final List<T> list;
        private final boolean isReversed;

        DescendingList(List<T> list) {
            if (!(list instanceof DescendingList)) {
                this.list = list;
                isReversed = true;
            } else {
                this.list = ((DescendingList<T>) list).list;
                isReversed = !((DescendingList<T>) list).isReversed;
            }
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public T get(int index) {
            return isReversed ? list.get(size() - index - 1) : list.get(index);
        }
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new DescendingList<>(sortedList), Collections.reverseOrder(comparator), true);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingList<>(sortedList).iterator();
    }

    @Override
    public boolean contains(Object o) {
        try {
            return Collections.binarySearch(sortedList, (E) o, comparator) >= 0;
        } catch (ClassCastException e) {
            return false;
        }
    }
}