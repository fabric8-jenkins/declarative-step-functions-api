package io.jenkins.functions;

import java.io.PrintStream;

public interface Logger {
    PrintStream out();
    PrintStream err();
}
