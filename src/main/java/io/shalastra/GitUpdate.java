package io.shalastra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static picocli.CommandLine.Command;

@Command(name = "gup", mixinStandardHelpOptions = true,
        version = "gup 0.0.1",
        description = "Updates all git repositories (current branch only) in the provided path.")
public class GitUpdate implements Callable<String> {

    @Override
    public String call() {
        String providedPath = ".";

        System.out.print(getClass().getPackage().getImplementationVersion());

        Path basePath = Path.of(providedPath);
        try (Stream<Path> paths = Files.list(basePath)) {
            paths.filter(Files::isDirectory).forEach(path -> {
                try {
                    Files.walkFileTree(path, Collections.emptySet(), 1,
                            new GitRepositorySearch(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
