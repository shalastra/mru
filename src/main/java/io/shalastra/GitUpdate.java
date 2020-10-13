package io.shalastra;

import picocli.CommandLine.Model.CommandSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static picocli.CommandLine.*;

@Command(name = "gup", mixinStandardHelpOptions = true,
        version = "gup 0.0.1",
        description = "Updates all git repositories (current branch only) in the provided path.")
public class GitUpdate implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", description = "The path to root folder containing git repositories.")
    private Path path;

    @Override
    public Integer call() throws IOException {
        List<Path> paths = Optional.of(collectDirectoryPaths()).orElse(Collections.emptyList());

        System.out.format("> Found %d repositories. Checking for updates...%n", paths.size());

        paths.forEach(p -> {
            try {
                Files.walkFileTree(p, Collections.emptySet(), 1,
                        new DirectoryVisitor(p));
            } catch (IOException e) {
                throw new ParameterException(spec.commandLine(), String.format("%s", e.getLocalizedMessage()));
            }
        });

        System.out.format("%n> Update successful, all repositories in %s are up to date.", path.subpath(0,
                path.getNameCount()));

        return 0;
    }

    private List<Path> collectDirectoryPaths() throws IOException {
        List<Path> paths;
        try (Stream<Path> pathStream = Files.list(path)) {
            paths = pathStream
                    .filter(Files::isDirectory)
                    .filter(file -> !file.toAbsolutePath().toString().contains(".vscode"))
                    .filter(file -> !file.toAbsolutePath().toString().contains(".metals"))
                    .collect(Collectors.toList());

        } catch (NoSuchFileException ex) {
            throw new ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for path - value is not a path.", path.toString()));
        }

        return paths;
    }
}
