package io.shalastra;

import picocli.CommandLine.Model.CommandSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.shalastra.GitUpdate.version;
import static picocli.CommandLine.*;

@Command(name = "mru", mixinStandardHelpOptions = true,
        version = version,
        description = "Updates all git repositories (current branch only) in the provided path.")
public class GitUpdate implements Callable<Integer> {

    public final static String version = "0.0.2.1";

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", description = "The path to root folder containing git repositories.")
    private Path path;

    @Override
    public Integer call() throws IOException {
        Optional.of(collectDirectoryPaths())
                .ifPresentOrElse(getListConsumer(),
                        () -> System.out.format("No directories in the provided path."));

        return 0;
    }

    private Consumer<List<Path>> getListConsumer() {
        return paths -> {
            System.out.format("> Found %d directories. Checking for git repositories...%n", paths.size());

            paths.forEach(this::discoverGitRepositories);

            System.out.format("%n> Update successful, all repositories in %s are up to date.", path.subpath(0,
                    path.getNameCount()));
        };
    }

    private List<Path> collectDirectoryPaths() throws IOException {
        List<Path> paths;
        try (Stream<Path> pathStream = Files.list(path)) {
            paths = getPaths(pathStream);

        } catch (NoSuchFileException ex) {
            throw new ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for path - value is not a path.", path.toString()));
        } catch (NotDirectoryException ex) {
            throw new ParameterException(spec.commandLine(), "You have to provide a directory.");
        }

        return paths;
    }

    private List<Path> getPaths(Stream<Path> pathStream) {
        List<Path> paths;
        paths = pathStream
                .filter(Files::isDirectory)
                .filter(file -> !file.toAbsolutePath().toString().contains(".vscode"))
                .filter(file -> !file.toAbsolutePath().toString().contains(".metals"))
                .collect(Collectors.toList());

        return paths;
    }

    private void discoverGitRepositories(Path p) {
        try {
            Files.walkFileTree(p, Collections.emptySet(), 1,
                    new DirectoryVisitor(p));
        } catch (IOException e) {
            throw new ParameterException(spec.commandLine(), String.format("%s", e.getLocalizedMessage()));
        }
    }
}
