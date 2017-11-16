/**
 * Copyright (C) Original Authors 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.functions.loader;

import io.jenkins.functions.Logger;
import io.jenkins.functions.support.DefaultLogger;

import java.io.File;
import java.io.PrintStream;

/**
 * Represents the context to invoke a function
 */
public class FunctionContext {
    private Logger logger;
    private File currentDir;

    public FunctionContext() {
        this.logger = DefaultLogger.getInstance();
        this.currentDir = new File(".");
    }

    public FunctionContext(Logger logger, File currentDir) {
        this.logger = logger;
        this.currentDir = currentDir;
    }

    public static FunctionContext newInstance(File currentDir, PrintStream out, PrintStream err) {
        Logger logger = new DefaultLogger(out, err);
        return new FunctionContext(logger, currentDir);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }
}
