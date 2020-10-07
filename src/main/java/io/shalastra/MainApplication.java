package io.shalastra;

import picocli.CommandLine;

public class MainApplication {

    /**
     * --info -i - shows full stack
     * --help -h - help
     * <p>
     * gup .
     * gup /home/Documents/Projects
     */

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GitUpdate()).execute(args);
        System.exit(exitCode);
    }
}

