package io.shalastra;

import picocli.CommandLine;

public class MainApplication {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GitUpdate()).execute(args);
        System.exit(exitCode);
    }
}

