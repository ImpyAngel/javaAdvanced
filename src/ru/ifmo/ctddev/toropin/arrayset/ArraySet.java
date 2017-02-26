package ru.ifmo.ctddev.toropin.arrayset;

import java.util.*;

/**
 * Created by impy on 17.02.17.
 */
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {

    private ArrayList<T> data;

    private Comparator<T> comp;

    public ArraySet(){
        this.data = new ArrayList<T>();
        this.comp = null;
    }

    public ArraySet(Collection<T> data) {
        this.data = new ArrayList<T>(data);
        this.data.sort(this.comp);
        this.comp = null;
    }

    public ArraySet(Collection<T> data, Comparator<T> comp) {
        this(data, comp, false);
    }

    private ArraySet(Collection<T> data, Comparator<T> comp, boolean sorted) {
        this.comp = comp;
        if (sorted) {
            this.data = new ArrayList<T>(data);
        } else {
            TreeSet<T> tree = new TreeSet<T>(comp);
            tree.addAll(data);
            this.data = new ArrayList<T>(tree);
            this.data.sort(this.comp);
        }
    }
    /*
        * hand is a turn of start the binarySearch
        * comp is a wrapper on the lambda
        * it return 1 when element necessary and 0 else
        * function return index of necessary element or -1
     */
    private int binarySearch(Comparator<Integer> comp, boolean hand) {
        if (this.size() == 0) return -1;

        int left = 0, right = data.size() - 1;
        if ( (comp.compare(left, left) == 0) ||
                (comp.compare(right, right) == 0)) return -1;
        if (hand && comp.compare(right, right) == 1) return right;
        if (!hand && comp.compare(left, left) == 1) return left;

        while (right - left > 1) {
            int middle = (left + right) / 2;
            if ((middle > 0 && middle < size() && comp.compare(middle, middle) == 1) ^ !hand) {
                left = middle;
            } else {
                right = middle;
            }
        }
        return hand ? left : right;
    }

    private T search(int index) {
        return index == -1 ? null : data.get(index);
    }

    private int indexLower(T t, boolean inclusive) {
       try {
           return binarySearch((t1, t2) -> comp.compare(data.get(t1), t) <= (inclusive ? 0 : -1) ? 1 : 0, true);
       } catch (NullPointerException e) {
            return 0;
       }
    }

    private int indexHigher(T t, boolean inclusive) {
    try {
            return binarySearch((t1, t2) -> comp.compare(data.get(t1), t) >= (inclusive ? 0 : 1) ? 1 : 0, false);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public T lower(T t) {
        return search(indexLower(t, false));
    }

    @Override
    public T floor(T t) { return search(indexLower(t, true)); }

    @Override
    public T ceiling(T t) { return search(indexHigher(t, true)); }

    @Override
    public T higher(T t) { return search(indexHigher(t,false)); }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("Immutable set");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("Immutable set");
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (T) o, comp) >= 0;
    }

    @Override
    public Iterator<T> iterator() {

        return Collections.unmodifiableList(data).iterator();
    }
    @Override
    public int size() {
        return data.size();
    }

//    @Override
//    public boolean containsAll(Collection<?> collection) {
//        for (Object aCollection : collection)
//            if (!contains(aCollection)) return false;
//        return true;
//    }
    @Override
    public NavigableSet<T> descendingSet() {
        ArrayList new_data = new ArrayList<T>(data);
        Collections.reverse(new_data);
        return new ArraySet<T>(new_data, Collections.reverseOrder(comp));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T t, boolean b, T e1, boolean b1) {
        return (this.tailSet(t, b)).headSet(e1, b1);
    }

    @Override
    public NavigableSet<T> tailSet(T t, boolean b) {
        int from = indexHigher(t, b);
        if (from < 0 || data.size() <= from)  {
            ArraySet<T> ans = new ArraySet<T>();
            ans.comp = comp;
            return ans;
        }
        return new ArraySet<T>(data.subList(from, data.size()), comp, true);
    }

    @Override
    public NavigableSet<T> headSet(T t, boolean b) {
        return new ArraySet<T>(data.subList(0, indexLower(t, b) + 1), comp, true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comp;
    }

    @Override
    public SortedSet<T> subSet(T t, T e1) {
        return subSet(t, true, e1, false);
    }

    @Override
    public SortedSet<T> headSet(T t) {
        return headSet(t, false);
    }

    @Override
    public SortedSet<T> tailSet(T t) {
        return tailSet(t, true);
    }

    @Override
    public T first() {
        if (size() == 0) throw new NoSuchElementException("Set is empty");
        return data.get(0);
    }

    @Override
    public T last() {
    if (size() == 0) throw new NoSuchElementException("Set is empty");
        return data.get(data.size() - 1);
    }
    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException("Immutable set");
    }
//
//    @Override
//    public boolean remove(Object o) {
//        throw new UnsupportedOperationException("Immutable set");
//    }
//
//
//    @Override
//    public boolean addAll(Collection<? extends T> collection) {
//        throw new UnsupportedOperationException("Immutable set");
//    }
//
//    @Override
//    public boolean retainAll(Collection<?> collection) {
//        throw new UnsupportedOperationException("Immutable set");
//    }
//
//    @Override
//    public boolean removeAll(Collection<?> collection) {
//        throw new UnsupportedOperationException("Immutable set");
//    }
//
//    @Override
//    public void clear() {
//        throw new UnsupportedOperationException("Immutable set");
//    }
//

//    @Override
//    public <T1> T1[] toArray(T1[] t1s) {
//        throw new UnsupportedOperationException("Immutable set");
//    }


}
