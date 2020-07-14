package ru.ifmo.rain.hakimov.walk;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;

public class HashFileVisitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter fileWriter;

    HashFileVisitor(BufferedWriter fileWriter) {
        this.fileWriter = fileWriter;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        hashFNV(file, fileWriter);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        log(fileWriter, 0, file.toString());
        return FileVisitResult.CONTINUE;
    }

    private void hashFNV(Path filePath, BufferedWriter bufferedWriter) throws IOException {
        final int FNV_PRIME = 0x01000193;
        final int BLOCK_SIZE = 7777;
        int hash = 0x811c9dc5;

        byte[] block = new byte[BLOCK_SIZE];
        int readBytesCount;
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            while ((readBytesCount = inputStream.read(block)) >= 0) {
                for (int i = 0; i < readBytesCount; ++i) {
                    hash *= FNV_PRIME;
                    hash ^= Byte.toUnsignedInt(block[i]);
                }
            }
        } catch (IOException e) {
            hash = 0;
        }

        log(bufferedWriter, hash, filePath.toString());
    }

    private void log(BufferedWriter bufferedWriter, int hashCode, String pathName) throws IOException {
        bufferedWriter.write(String.format("%08x", hashCode) + " " + pathName);
        bufferedWriter.newLine();
    }

    /* package-private */ void hashFile(String pathName, BufferedWriter bufferedWriter, HashFileVisitor hashVisitor) throws IOException {
        try {
            Path currentPath = Paths.get(pathName);

            if (Files.isDirectory(currentPath)) {
                Files.walkFileTree(currentPath, hashVisitor);
            } else {
                hashVisitor.hashFNV(currentPath, fileWriter);
            }
        } catch (InvalidPathException e) {
            hashVisitor.log(fileWriter, 0, pathName);
        }
    }
}
