package ru.ifmo.ctddev.toropin.concurrent;


import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Integer.max;

/**
 * Created by impy on 19.03.17.
 */
public class Scalar implements ListIP {

    private ParallelMapper mapper = null;

    public Scalar(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        Function<List<?>, String> joiner = objects ->
                objects.stream().map(Object::toString).collect(Collectors.joining());
        return parallelWork(i, list, joiner, joiner);
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, List<T>> filt = ts -> ts.stream().filter(predicate).collect(Collectors.toList());
        Function<List<? extends List<T>>, List<T>> listJoiner = lists ->
                lists.stream().flatMap(Collection::stream).collect(Collectors.toList());
        return parallelWork(i, list, filt, listJoiner);
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        Function<List<? extends T>, List<U>> mapper = ts -> ts.stream().map(function).collect(Collectors.toList());
        Function<List<? extends List<U>>, List<U>> listJoiner = lists ->
                lists.stream().flatMap(Collection::stream).collect(Collectors.toList());
        return parallelWork(i, list, mapper, listJoiner);
    }

    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> max = chunk -> Collections.max(chunk, comparator);
        return parallelWork(i, list, max, max);
    }

    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(i, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> al = chunk -> chunk.stream().allMatch(predicate);
        return parallelWork(i, list, al, chunk -> chunk.stream().allMatch(x -> x));
    }

    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !all(i, list, predicate.negate());
    }

    private <T, R> R parallelWork(int threads, List<? extends T> data,
                                  final Function<List<? extends T>, R> onChunk,
                                  final Function<? super List<R>, R> finalizer) throws InterruptedException {
        final List<List<? extends T>> list = split(data, threads);

        if (mapper != null) {
            //Doing with mapper
            return finalizer.apply(mapper.map(onChunk, list));
        }

        final List<R> results = new ArrayList<>(list.size());
        final List<Thread> tasks = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            final int ic = i;
            results.add(null);
            tasks.add(new Thread(() -> {
                R res = onChunk.apply(list.get(ic));
                synchronized (results) {
                    results.set(ic, res);
                }
            }));
            tasks.get(ic).start();
        }
        for (Thread t : tasks) {
            t.join();
        }
        return finalizer.apply(results);
    }

    private <U> List<List<? extends U>> split(List<? extends U> list, int i) {
        int itemsPerOne = (list.size() + i - 1) / i;
        List<List<? extends U>> res = new ArrayList<>();
        for (int j = 0; j < list.size(); j += itemsPerOne) {
            res.add(list.subList(j, Math.min(j + itemsPerOne, list.size())));
        }
        return res;
    }
}
