package io.shalastra;

import java.net.URI;

public class GitRepository {

    private final String name;
    private final URI path;

    public GitRepository(String name, URI path) {
        this.name = name;
        this.path = path;
    }

    public String name() {
        return name;
    }

    public URI path() {
        return path;
    }
}
