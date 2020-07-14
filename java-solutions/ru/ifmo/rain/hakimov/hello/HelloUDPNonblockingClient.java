package ru.ifmo.rain.hakimov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.DatagramChannel;
import java.net.InetSocketAddress;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static ru.ifmo.rain.hakimov.hello.Utils.*;

/**
 * @author Muhammadjon Hakimov
 * Created on 14/04/2020 02:57:52
 */
public class HelloUDPNonblockingClient implements HelloClient {
    InetSocketAddress socketAddress;

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
        try (Selector selector = Selector.open()) {
            socketAddress = new InetSocketAddress(host, port);

            for (int i = 0; i < threads; i++) {
                try {
                    final DatagramChannel channel = DatagramChannel.open();

                    channel.configureBlocking(false);
                    channel.register(selector, OP_WRITE, new TransferData(prefix, i, requests, channel));
                } catch (IOException e) {
                    System.err.println("ERROR: Something wrong occurred while I/O!\n" + e.getMessage());
                    return;
                }
            }

            int loaded = 0;
            while (loaded < threads) {
                try {
                    selector.select(100);

                    if (selector.selectedKeys().isEmpty()) {
                        selector.keys().forEach(key -> key.interestOps(OP_WRITE));
                    }

                    for (final var i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                        final SelectionKey key = i.next();
                        final TransferData data = (TransferData) key.attachment();

                        if (data.requests == data.requestId) {
                        	try {
				                data.channel.close();
				            } catch (IOException ignored) {
				                // No operations
				            }

                            ++loaded;
                        } else if (data.requests > data.requestId) {
                            if (key.isReadable()) {
                                try {
                                    data.buffer.clear();
                                    data.channel.receive(data.buffer);
                                    data.buffer.flip();

                                    final String responseMessage = getStringFromBuffer(data.buffer);
                                    if (checkResponse(data.prefix, data.threadId, data.requestId, responseMessage)) {
                                        data.requestId++;
                                    }
                                } catch (Exception e) {
                                    System.err.println("ERROR: Unable to receive!");
                                }

                                if (data.channel.isOpen()) {
                                    key.interestOps(SelectionKey.OP_WRITE);
                                }
                            } else if (key.isWritable()) {
                                try {
                                    final String requestMessage =
                                            getRequestMessage(data.prefix, data.threadId, data.requestId);

                                    data.buffer.clear();
                                    data.buffer.put(requestMessage.getBytes());
                                    data.buffer.flip();
                                    data.channel.send(data.buffer, socketAddress);
                                } catch (IOException e) {
                                    System.err.println("ERROR: Unable to send!");
                                }

                                if (data.channel.isOpen()) {
                                    key.interestOps(OP_READ);
                                }
                            }

                            i.remove();
                        }
                    }
                } catch (IOException e) {
                    System.err.println("ERROR: " + e.getMessage());

                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR: Unable to close Selector!");
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

        (new HelloUDPNonblockingClient()).run(host, port, prefix, threads, requests);
    }
}
