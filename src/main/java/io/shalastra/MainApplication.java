package io.shalastra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

public class MainApplication {

    public static void main(String[] args) {
        System.out.println(args[0]);
        System.out.println("You have to enter the right path");

        Path basePath = Path.of("/home/szymon/Documents/Projects/zio-projects");
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
    }
}

