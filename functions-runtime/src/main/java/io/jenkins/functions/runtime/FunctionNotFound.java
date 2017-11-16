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
package io.jenkins.functions.runtime;

/**
 * Thrown if a function cannot be found
 */
public class FunctionNotFound extends Exception {
    private final String functionName;

    public FunctionNotFound(String functionName) {
        super("No function called " + functionName + " could be found");
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }
}
