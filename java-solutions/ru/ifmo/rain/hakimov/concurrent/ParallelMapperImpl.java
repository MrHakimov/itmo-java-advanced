package ru.ifmo.rain.hakimov.concurrent;

import info.kgeorgiy.java.advanced.mapper.*;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final static int MAX_SIZE = 100000;
    private final Queue<Runnable> tasks;
    private final List<Thread> threads;

    public ParallelMapperImpl(final int threadsNumber) {
        threads = new ArrayList<>();
        tasks = new ArrayDeque<>();

        Runnable worker = () -> {
            try {
                while (!Thread.interrupted()) {
                    Runnable task;
                    synchronized (tasks) {
                        while (tasks.isEmpty()) {
                            tasks.wait();
                        }

                        task = tasks.poll();
                        tasks.notifyAll();
                    }
                    task.run();
                }
            } catch (InterruptedException ignored) {
                // No operations
            } finally {
                Thread.currentThread().interrupt();
            }
        };

        for (int i = 0; i < threadsNumber; i++) {
            Thread thread = new Thread(worker);
            threads.add(thread);
            thread.start();
        }
    }

    private class ResultCollector<R> {
        private int alreadyCollected;
        private final List<R> result;

        ResultCollector(final int size) {
            alreadyCollected = 0;
            result = new ArrayList<>(Collections.nCopies(size, null));
        }

        synchronized void setData(final int index, R data) {
            result.set(index, data);

            if (++alreadyCollected == result.size()) {
                notify();
            }
        }

        synchronized List<R> getResult() throws InterruptedException {
            while (alreadyCollected < result.size()) {
                wait();
            }

            return result;
        }
    }

    private void add(final Runnable task) throws InterruptedException {
        synchronized (tasks) {
            while (tasks.size() == MAX_SIZE) {
                tasks.wait();
            }

            tasks.add(task);
            tasks.notifyAll();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        ResultCollector<R> resultCollector = new ResultCollector<>(list.size());
        
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            add(() -> resultCollector.setData(index, function.apply(list.get(index))));
        }
        return resultCollector.getResult();
    }

    @Override
    public void close() {
        threads.forEach(Thread::interrupt);

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
                // No operations
            }
        }
    }
}
