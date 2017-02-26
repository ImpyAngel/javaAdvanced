package ru.ifmo.ctddev.toropin.arrayset;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        TreeSet<Integer> set = new TreeSet<Integer>();
        set.add(1);
        set.add(2);
        set.add(7);
        set.add(4);
        set.add(5);
        ArraySet<Integer> test = new ArraySet<Integer>(set, Integer::compareTo);
        System.out.println(test.floor(6));
        System.out.println(test.ceiling(6));
        System.out.println(test.contains(4));
        System.out.println(test.containsAll(set));
        NavigableSet<Integer> new_test = test.tailSet(null, true);
        System.out.println(new_test.size());
    }
}
