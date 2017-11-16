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
package io.jenkins.functions.support;

import io.jenkins.functions.Logger;

import java.io.PrintStream;

/**
 */
public class DefaultLogger extends Logger {
    private static Logger instance = new DefaultLogger(System.out, System.err);
    private final PrintStream out;
    private final PrintStream err;

    public DefaultLogger(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    public static Logger getInstance() {
        return instance;
    }

    @Override
    public PrintStream out() {
        return out;
    }

    @Override
    public PrintStream err() {
        return err;
    }
}
