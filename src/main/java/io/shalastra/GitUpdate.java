package io.shalastra;

import picocli.CommandLine.Model.CommandSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static picocli.CommandLine.*;

@Command(name = "gup", mixinStandardHelpOptions = true,
        version = "gup 0.0.1",
        description = "Updates all git repositories (current branch only) in the provided path.")
public class GitUpdate implements Callable<String> {

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", description = "The path to root folder containing git repositories.")
    private Path path;

    @Override
    public String call() {
        try (Stream<Path> paths = Files.list(path)) {
            paths.filter(Files::isDirectory).forEach(path -> {
                try {
                    Files.walkFileTree(path, Collections.emptySet(), 1,
                            new DirectoryVisitor(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (NoSuchFileException ex) {
            throw new ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for path: " +
                            "value is not a path.", path.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int repositoriesCount = 0;
        return String.format("Update successful, %d updated  repositories.", repositoriesCount);
    }
}
