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

import hudson.Extension;
import io.jenkins.functions.loader.StepFunction;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import java.util.Map;

/**
 * An example of the step class we should code generate for each step function so that we can reuse the steps inside
 * either the groovy scripted pipeline engine or declarative pipelines
 */
public class MyStep extends StepSupport {
    public static final String STEP_FUNCTION_NAME = "mystep";
    public static final String STEP_DISPLAY_NAME = "My Step";

    public MyStep(StepFunction function) {
        super(function);
    }

    // getter / setter on properties to attributes

    public String getCheese() {
        return getArgument("cheese", String.class);
    }

    public void setCheese(String cheese) {
        setArgument("cheese", cheese);
    }


    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new ExecutionSupport(function(), arguments(), context);
    }

    @Extension
    public static class DescriptorImpl extends DescriptorSupport {
        public DescriptorImpl() {
            super(STEP_FUNCTION_NAME, STEP_DISPLAY_NAME);
        }
    }

    public static class Execution extends ExecutionSupport<Void> {
        public Execution(StepFunction function, Map<String, Object> arguments, StepContext context) {
            super(function, arguments, context);
        }
    }
}
