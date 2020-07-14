package ru.ifmo.rain.hakimov.crawler;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("SpellCheckingInspection")
class Utils {
    private final ExecutorService downloaders;
    final Queue<Runnable> tasksQueue;
    private final int perHost;
    int loaded = 0;

    Utils(final ExecutorService downloaders, final int perHost) {
        this.downloaders = downloaders;
        this.perHost = perHost;
        tasksQueue = new ArrayDeque<>();
    }

    synchronized void addTask(Runnable task) {
        if (loaded < perHost) {
            loaded++;
            downloaders.submit(task);
        } else {
            tasksQueue.add(task);
        }
    }
}
