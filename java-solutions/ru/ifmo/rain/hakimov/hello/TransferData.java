package ru.ifmo.rain.hakimov.hello;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

class TransferData {
    final ByteBuffer buffer = ByteBuffer.allocate(1024);
    int requestId;
    final String prefix;
    final int threadId;
    final int requests;
    final DatagramChannel channel;

    TransferData(final String prefix, final int threadId, final int requests, final DatagramChannel channel) {
        this.threadId = threadId;
        this.requests = requests;
        this.prefix = prefix;
        this.channel = channel;
    }
}
