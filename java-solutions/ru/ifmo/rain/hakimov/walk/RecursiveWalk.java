package ru.ifmo.rain.hakimov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {
    private static void recursiveWalk(Path inputPath, Path outputPath) {
        try (BufferedReader fileReader = Files.newBufferedReader(inputPath)) {
            try (BufferedWriter fileWriter = Files.newBufferedWriter(outputPath)) {
                HashFileVisitor hashVisitor = new HashFileVisitor(fileWriter);
                String pathName;

                while ((pathName = fileReader.readLine()) != null) {
                    hashVisitor.hashFile(pathName, fileWriter, hashVisitor);
                }
            } catch (IOException e) {
                System.err.println("Unable to write to output file: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Unable to read from input file: " + e.getMessage());
        }
    }

    private static boolean invalidArguments(String[] args) {
        return args == null || args.length != 2 || args[0] == null || args[1] == null;
    }

    public static void main(String[] args) {
        if (invalidArguments(args)) {
            System.err.println("USAGE: java RecursiveWalk <input_file_path> <output_file_path>");
        } else {
            Path inputFileName;
            Path outputFileName;

            try {
                inputFileName = Paths.get(args[0]);
                outputFileName = Paths.get(args[1]);

                Path parentDir = outputFileName.getParent();

                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectory(parentDir);
                }

                recursiveWalk(inputFileName, outputFileName);
            } catch (InvalidPathException | IOException e) {
                System.err.println("Unable to create one of the output file's parent directories: " + e.getMessage());
            }
        }
    }
}
