package ru.ifmo.ctddev.toropin.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

/**
 * Created by impy on 31.03.17.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private List<Thread> employees;
    private Queue<Runnable> conqurencyQueue;

    ParallelMapperImpl() {}

    ParallelMapperImpl(int threads) {
        employees = new ArrayList<>(threads);
        Runnable therad = new Runnable() {
            @Override
            public void run() {
                synchronized (conqurencyQueue) {
                    while(!conqurencyQueue.isEmpty()) {
                        try {
                            Runnable temp;
                            synchronized (conqurencyQueue) {
                                while(!conqurencyQueue.isEmpty()) {
                                    conqurencyQueue.wait();
                                }
                                temp = conqurencyQueue.remove();
                            }
                            temp.run();
                        } catch (InterruptedException ignored) {

                        }
                        finally {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        };
        for (int i = 0; i < threads; ++i) {
            employees.add(new Thread());
            employees.get(i).start();
        }
    }
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Stay<R> stay = new Stay<>(args.size());
        synchronized (conqurencyQueue) {
            for (int i = 0; i < args.size(); ++i) {
                int j = i;
                conqurencyQueue.add(() -> stay.set(j, f.apply(args.get(j))));
                conqurencyQueue.notify();
            }
        }
        return stay.get();
    }
    @Override
    public void close() throws InterruptedException {
        employees.forEach(Thread::interrupt);
        for (Thread t : employees) {
            t.join();
        }
    }

    private class Stay<T> {
        private List<T> result;
        private int c = 0;
        Stay(int size) {
            this.result = new ArrayList<T>(Collections.nCopies(size, null));
            c = 0;
        }
        synchronized void set(int pos, T value) {
            result.set(pos, value);
            c++;
            if (c == result.size()) {
                this.notify();
            }
        }
        synchronized List<T> get() throws InterruptedException {
            while (c < result.size()) {
                this.wait();
            }
            return  result;
        }
    }
}
