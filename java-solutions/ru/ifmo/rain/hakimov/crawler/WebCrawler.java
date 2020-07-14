package ru.ifmo.rain.hakimov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Muhammadjon Hakimov
 * Created on 01/04/2020 03:34:01
 */
@SuppressWarnings("SpellCheckingInspection")
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;
    private final ConcurrentMap<String, Utils> hosts;

    /**
     * Class constructor
     *
     * @param downloader  downloader
     * @param downloaders threads number for downloading
     * @param extractors  threads number for extracting links
     * @param perHost     threads number for downloading from the same host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        this.hosts = new ConcurrentHashMap<>();
    }

    private void recursiveDownload(String url, Phaser sync,
                                   Set<String> result, Set<String> usedLinks,
                                   int depth, ConcurrentMap<String, IOException> exceptions) {
        try {
            String host = URLUtils.getHost(url);
            Utils utils = hosts.computeIfAbsent(host, s -> new Utils(downloaders, perHost));

            sync.register();
            utils.addTask(() -> {
                try {
                    Document downloaded = downloader.download(url);
                    result.add(url);

                    if (depth != 1) {
                        sync.register();

                        Runnable extractorTask = () -> {
                            try {
                                for (String link : downloaded.extractLinks()) {
                                    if (usedLinks.add(link)) {
                                        recursiveDownload(link, sync, result, usedLinks, depth - 1, exceptions);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                sync.arrive();
                            }
                        };

                        extractors.submit(extractorTask);
                    }
                } catch (IOException e) {
                    exceptions.put(url, e);
                } finally {
                    sync.arrive();

                    synchronized (utils) {
                        Runnable otherTask = utils.tasksQueue.poll();

                        if (otherTask != null) {
                            downloaders.submit(otherTask);
                        } else {
                            utils.loaded--;
                        }
                    }
                }
            });
        } catch (MalformedURLException e) {
            exceptions.put(url, e);
        }
    }

    /**
     * Gets list of all URLs
     *
     * @param url   starting URL of crawler
     * @param depth depth of pages for visiting
     * @return {@link info.kgeorgiy.java.advanced.crawler.Result}, containing list of all downloaded pages and errors
     */
    @Override
    public Result download(String url, int depth) {
        Phaser sync = new Phaser(1);

        Set<String> result = ConcurrentHashMap.newKeySet();

        Set<String> usedLinks = ConcurrentHashMap.newKeySet();
        usedLinks.add(url);

        ConcurrentMap<String, IOException> exceptions = new ConcurrentHashMap<>();

        recursiveDownload(url, sync, result, usedLinks, depth, exceptions);
        sync.arriveAndAwaitAdvance();

        return new Result(new ArrayList<>(result), exceptions);
    }

    private static boolean invalidArguments(String[] args) {
        if (args == null || args.length < 1 || args.length > 4) {
            return true;
        }

        for (String arg : args) {
            if (arg == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Main function
     *
     * @param args array of arguments
     */
    public static void main(String[] args) {
        if (invalidArguments(args)) {
            System.err.println("Incorrect usage!\nUsage: WebCrawler url [downloaders [extractors [perHost]]]");
            return;
        }

        final String url = args[0];
        final int downloaders = (args.length <= 1 || args[1] == null ? 4 : Integer.parseInt(args[1]));
        final int extractors = (args.length <= 2 || args[2] == null ? 4 : Integer.parseInt(args[2]));
        final int perHost = (args.length <= 3 || args[3] == null ? 3 : Integer.parseInt(args[3]));
        final int depth = (args.length <= 4 || args[4] == null ? 2 : Integer.parseInt(args[4]));

        try {
            Downloader downloader = new CachingDownloader();
            WebCrawler webCrawler = new WebCrawler(downloader, downloaders, extractors, perHost);
            webCrawler.download(url, depth);
        } catch (IOException e) {
            System.err.println("Error occurred while creating Downloader" +
                    " or downloading from the given url: " + e.getMessage());
        }
    }

    /**
     * Shutdowns all threads.
     */
    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }
}
