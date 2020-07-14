package ru.ifmo.rain.hakimov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.ifmo.rain.hakimov.hello.Utils.getServerUsageMessage;
import static ru.ifmo.rain.hakimov.hello.Utils.invalidArguments;

/**
 * @author Muhammadjon Hakimov
 * Created on 14/04/2020 03:32:25
 */
public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService receiver;
    private ExecutorService workers;

    /**
     * Starts a new Hello server.
     *
     * @param port    server port.
     * @param threads number of working threads.
     */
    public void start(int port, int threads) {
        receiver = Executors.newSingleThreadExecutor();
        workers = Executors.newFixedThreadPool(threads);

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.err.println("ERROR: Unable to connect to port " + port + " and create a socket!");
            return;
        }

        receiver.submit(() -> {
            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    final byte[] data = new byte[socket.getReceiveBufferSize()];
                    DatagramPacket packet = new DatagramPacket(data, data.length);

                    socket.receive(packet);

                    workers.submit(() -> {
                        final String responseInit = new String(packet.getData(), packet.getOffset(), packet.getLength());
                        final byte[] responseBytes = ("Hello, " + responseInit).getBytes();
                        DatagramPacket response = new DatagramPacket(responseBytes, responseBytes.length, packet.getSocketAddress());

                        try {
                            socket.send(response);
                        } catch (IOException e) {
                            System.err.println("ERROR: I/O exception occurred while sending: " + e.getMessage());
                        }
                    });
                } catch (IOException ignored) {
                    // No operations
                }
            }
        });
    }

    /**
     * Usage: <port> <number of threads>
     *
     * @param args defines an array of arguments: {@link Integer} port, {@link Integer} threads.
     */
    public static void main(String[] args) {
        if (invalidArguments(args, 2)) {
            System.err.println(getServerUsageMessage());
        }

        final int port = Integer.parseInt(args[0]);
        final int threads = Integer.parseInt(args[1]);

        (new HelloUDPServer()).start(port, threads);
    }

    /**
     * Stops server and frees all resources.
     */
    @Override
    public void close() {
        socket.close();
        receiver.shutdown();
        workers.shutdown();

        try {
            workers.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // No operations
        }
    }
}
