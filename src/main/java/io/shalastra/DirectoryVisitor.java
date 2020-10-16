package io.shalastra;

import com.diogonunes.jcolor.Attribute;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.diogonunes.jcolor.Ansi.colorize;

public class DirectoryVisitor implements FileVisitor<Path> {

    private static final String GIT_REPOSITORY = ".git";

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

        if (GIT_REPOSITORY.equals(current)) {
            System.out.format("> Checking %s for updates", file.subpath(file.getNameCount() - 2,
                    file.getNameCount() - 1));

            Optional.of(updateRepository())
                    .filter(line -> !line.contains("Already up to date"))
                    .ifPresentOrElse(s -> {
                                System.out.format(" ------> ");
                                System.out.format(colorize("CHANGES DETECTED", Attribute.BOLD(),
                                        Attribute.YELLOW_TEXT()));
                                System.out.format("%n");
                            },
                            () -> {
                                System.out.format(" ------> ");
                                System.out.format(colorize("UP-TO-DATE", Attribute.BOLD(), Attribute.GREEN_TEXT()));
                                System.out.format("%n");
                            });

            return FileVisitResult.TERMINATE;
        }
        return FileVisitResult.CONTINUE;
    }

    private String updateRepository() throws IOException {
        BufferedReader output = new BufferedReader(new InputStreamReader(executeCommand()));

        return output.lines().collect(Collectors.joining());
    }

    private InputStream executeCommand() throws IOException {
        final String SHELL = "/bin/bash";
        final String EXECUTABLE = "-c";
        final String GIT_PULL = "git pull";

        ProcessBuilder builder = new ProcessBuilder(SHELL, EXECUTABLE, GIT_PULL);
        builder.directory(new File(basePath.toUri()));
        Process process = builder.start();

        return process.getInputStream();
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
