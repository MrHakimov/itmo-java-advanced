package ru.ifmo.rain.hakimov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.*;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ScalarIP, ListIP {
    private final ParallelMapper parallelMapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.parallelMapper = mapper;
    }

    public IterativeParallelism() {
        parallelMapper = null;
    }

    private <T, R> R baseTask(int threadNumber, final List<? extends T> list,
                              final Function<? super Stream<? extends T>, ? extends R> mapper,
                              final Function<? super Stream<? extends R>, ? extends R> resultCollector) throws InterruptedException {
        if (threadNumber <= 0) {
            throw new IllegalArgumentException("Incorrect number of threads (<= 0)!");
        }

        final List<Stream<? extends T>> subTasks = new ArrayList<>();
        
        threadNumber = Math.max(1, Math.min(threadNumber, list.size()));
        final int blockSize = list.size() / threadNumber;
        int carry = list.size() % threadNumber;
        
        int leftBound = 0;

        for (int i = 0; i < threadNumber; i++) {
            final int left = leftBound;
            final int right = Math.min(list.size(), left + blockSize + (carry-- > 0 ? 1 : 0));
            
            subTasks.add(list.subList(left, right).stream());
            
            leftBound = right;
        }

        final List<R> result;

        if (parallelMapper == null) {
            final List<Thread> threads = new ArrayList<>();
            result = new ArrayList<>(Collections.nCopies(subTasks.size(), null));

            for (int i = 0; i < threadNumber; ++i) {
                final int index = i;

                Thread thread = new Thread(() -> result.set(index, mapper.apply(subTasks.get(index))));
                threads.add(thread);
                thread.start();
            }

            boolean failedToJoin = false;
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException ignored) {
                    failedToJoin = true;
                    break;
                }
            }

            if (failedToJoin) {
                throw new InterruptedException("One of the threads failed to join");
            }
        } else {
            result = parallelMapper.map(mapper, subTasks);
        }

        return resultCollector.apply(result.stream());
    }

    @Override
    public <T> T minimum(int threadNumber, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        if (list.size() == 0) {
            throw new IllegalArgumentException("Non zero list size expected");
        }

        return baseTask(threadNumber, list,
                stream -> stream.min(comparator).get(),
                stream -> stream.min(comparator).get());
    }

    @Override
    public <T> T maximum(int threadNumber, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return minimum(threadNumber, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threadNumber, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return baseTask(threadNumber, list,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(int threadNumber, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return baseTask(threadNumber, list,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
    }

    @Override
    public String join(int threadNumber, List<?> list) throws InterruptedException {
        return baseTask(threadNumber, list,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threadNumber, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return baseTask(threadNumber, list,
                stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threadNumber, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return baseTask(threadNumber, list,
                stream -> stream.map(function).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }
}
