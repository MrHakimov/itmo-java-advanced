package ru.ifmo.rain.hakimov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.ifmo.rain.hakimov.hello.Utils.*;

/**
 * @author Muhammadjon Hakimov
 * Created on 14/04/2020 02:57:52
 */
public class HelloUDPClient implements HelloClient {
    /**
     * Runs Hello client.
     *
     * @param host     server host
     * @param port     server port
     * @param prefix   request prefix
     * @param threads  number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        final InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        final ExecutorService workers = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            final int threadId = i;

            workers.submit(new Thread(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    final int bufferSize = socket.getReceiveBufferSize();
                    byte[] dataResponse = new byte[bufferSize];
                    DatagramPacket response = new DatagramPacket(dataResponse, bufferSize);

                    for (int requestId = 0; requestId < requests; requestId++) {
                        String request = getRequestMessage(prefix, threadId, requestId);
                        byte[] dataRequest = request.getBytes();
                        DatagramPacket packet = new DatagramPacket(dataRequest, request.length(), socketAddress);

                        socket.setSoTimeout(100);

                        boolean haveResponse = false;
                        while (!haveResponse) {
                            try {
                                socket.send(packet);
                                socket.receive(response);
                            } catch (SocketTimeoutException e) {
                                System.err.println("ERROR: The request has timed out: " + e.getMessage());
                            } catch (IOException e) {
                                System.err.println("ERROR: I/O exception occurred while sending: " + e.getMessage());
                            }

                            String result = new String(response.getData(), response.getOffset(), response.getLength());

                            if (result.contains(request)) {
                                System.out.println(result);
                                haveResponse = true;
                            }
                        }
                    }
                } catch (SocketException e) {
                    System.err.println("ERROR: the socket couldn't be opened, or" +
                            " bind to the specified port." + e.getMessage());
                }
            }));
        }

        workers.shutdown();

        try {
            workers.awaitTermination(threads * requests * 3, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
            // No operations
        }
    }

    /**
     * Runs {@link HelloUDPClient}.
     * <p>
     * Usage: <host> <prefix> <port> <threads> <requests>.
     *
     * @param args Array of arguments: {@link String} host, {@link String} prefix,
     *             {@link Integer} threads, {@link Integer} requests.
     */
    public static void main(String[] args) {
        if (invalidArguments(args, 5)) {
            System.err.println(getClientUsageMessage());
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String prefix = args[2];
        int threads = Integer.parseInt(args[3]);
        int requests = Integer.parseInt(args[4]);

        (new HelloUDPClient()).run(host, port, prefix, threads, requests);
    }
}
