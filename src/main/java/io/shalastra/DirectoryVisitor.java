package io.shalastra;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.stream.Collectors;

public class DirectoryVisitor implements FileVisitor<Path> {

    private final Path basePath;

    DirectoryVisitor(final Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String current = file.getFileName().toString();

        String absolutePath = basePath.toString();
        if (".git".equals(current)) {
            String command = "git pull";

            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
            builder.directory(new File(basePath.toUri()));
            Process process = builder.start();
            BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String gitOutput = output.lines().collect(Collectors.joining());

            Optional.of(gitOutput)
                    .filter(line -> line.contains("Already up tp date"))
                    .ifPresentOrElse(s -> System.out.format("------> CHANGES DETECTED%n"),
                            () -> System.out.format("------> UP-TO-DATE%n"));

            return FileVisitResult.TERMINATE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        boolean isFinished = Files.isSameFile(dir, basePath);

        return isFinished ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
    }
}
