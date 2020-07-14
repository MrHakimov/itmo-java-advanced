package ru.ifmo.rain.hakimov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static ru.ifmo.rain.hakimov.hello.Utils.*;

/**
 * @author Muhammadjon Hakimov
 * Created on 14/04/2020 03:32:25
 * <p>
 * Implementation of {@link HelloServer}.
 */
public class HelloUDPNonblockingServer implements HelloServer {
    
    //finals?
    private DatagramChannel channel;
    private ExecutorService receiver;
    private Selector selector;

    /**
     * Starts new {@link HelloUDPNonblockingServer}
     *
     * @param port    server port.
     * @param threads threads number of working threads.
     */
    public void start(int port, int threads) {
        try {
            receiver = Executors.newSingleThreadExecutor();
            channel = DatagramChannel.open();
            selector = Selector.open();
        } catch (IOException e) {
            System.err.println("ERROR: Unable to open selector/channel at port: " + port + "!");
            return;
        }

        final InetSocketAddress socketAddress = new InetSocketAddress(port);
        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        receiver.submit(() -> {
            try {
                channel.bind(socketAddress);
                channel.configureBlocking(false);
                channel.register(selector, OP_READ);

                SocketAddress address = socketAddress;

                while (selector.isOpen() && !Thread.interrupted()) {
                    selector.select(100);

                    for (final var i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                        final SelectionKey key = i.next();

                        if (key.isReadable()) {
                            buffer.clear();
                            address = channel.receive(buffer);
                            buffer.flip();

                            final String responseInit = getStringFromBuffer(buffer);
                            byte[] responseData = ("Hello, " + responseInit).getBytes();
                            buffer.clear();
                            buffer.put(responseData);
                            buffer.flip();

                            key.interestOps(OP_WRITE);
                        } else if (key.isWritable()) {
                            channel.send(buffer, address);
                            key.interestOps(OP_READ);
                        }

                        i.remove();
                    }
                }
            } catch (IOException e) {
                System.err.println("ERROR: Unable to connect to the specified port!");
            }
        });
    }

    /**
     * Main function for starting {@link HelloUDPNonblockingServer}.
     * <p>
     * Usage: <port> <number of threads.
     *
     * @param args array of arguments: {@link Integer} port, {@link Integer} threads.
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
        receiver.shutdownNow();

        try {
            channel.close();
            selector.close();
        } catch (IOException e) {
            System.err.println("ERROR: Unable to close datagram channel or selector!");
        }
    }
}
