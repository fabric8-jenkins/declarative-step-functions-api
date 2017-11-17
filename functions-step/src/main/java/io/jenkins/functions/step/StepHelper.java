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
package io.jenkins.functions.step;

import hudson.FilePath;
import hudson.model.TaskListener;
import io.jenkins.functions.Logger;
import io.jenkins.functions.runtime.FunctionContext;
import io.jenkins.functions.runtime.StepFunction;
import io.jenkins.functions.support.DefaultLogger;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 */
public class StepHelper {
    public static FunctionContext createFunctionContext(StepContext stepContext) throws IOException, InterruptedException {
        Logger logger = createLogger(stepContext);
        FunctionContext functionContext = new FunctionContext();
        functionContext.setLogger(logger);
        try {
            FilePath filePath = stepContext.get(FilePath.class);
            File currentDir = new File(filePath.toURI());
            functionContext.setCurrentDir(currentDir);
        } catch (Exception e) {
            logger.warn("Failed to get current directory: " + e, e);
        }
        return functionContext;
    }

    public static Logger createLogger(StepContext stepContext) throws IOException, InterruptedException {
        PrintStream out = stepContext.get(TaskListener.class).getLogger();
        return new DefaultLogger(out, out);
    }

    public static Object invokeFunction(StepFunction function, Map<String, Object> arguments, StepContext stepContext) throws IOException, InterruptedException {
        FunctionContext functionContext = createFunctionContext(stepContext);
        return function.invoke(arguments, functionContext);
    }

    public static Map<String, Object> getAllArguments(StepFunction function, Map<String, Object> arguments) {
        return function.getArguments(arguments, new FunctionContext());
    }
}
