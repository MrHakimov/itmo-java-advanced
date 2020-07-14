package ru.ifmo.rain.hakimov.hello;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author Muhammadjon Hakimov
 * Created on 02/06/2020 05:01:52
 */

class Utils {
    static boolean invalidArguments(String[] args, int expectedLength) {
        if (args == null || args.length != expectedLength) {
            return true;
        }

        for (int i = 0; i < expectedLength; i++) {
            if (args[i] == null) {
                return true;
            }
        }

        return false;
    }

    static String getRequestMessage(String prefix, int threadId, int requestId) {
        return prefix + threadId + "_" + requestId;
    }

    static String getStringFromBuffer(ByteBuffer buffer) {
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    static boolean checkResponse(String prefix, int threadId, int requestId, String response) {
        return response.contains(getRequestMessage(prefix, threadId, requestId));
    }

    static String getClientUsageMessage() {
        return "Usage: <host> <prefix> <port> <threads> <requests>\nArguments must be non-null.";
    }

    static String getServerUsageMessage() {
        return "Usage: <port> <number of threads>\nArguments must be non-null.";
    }
}
