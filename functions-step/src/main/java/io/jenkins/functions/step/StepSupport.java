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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.TaskListener;
import io.jenkins.functions.loader.StepFunction;
import io.jenkins.functions.loader.StepFunctions;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A base class for generating step function steps
 */
public abstract class StepSupport extends Step {

    private final StepFunction function;
    private final Map<String, Object> arguments = new HashMap<>();

    public StepSupport(StepFunction function) {
        this.function = function;
    }

    public StepSupport(String functionName, Class<?> clazz) {
        this(StepFunctions.loadFunction(functionName, clazz));
    }


    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new ExecutionSupport(function, arguments, context);
    }

    /**
     * Returns the function for this step
     */
    public StepFunction function() {
        return function;
    }

    /**
     * Returns the arguments for this step
     */
    public Map<String, Object> arguments() {
        return Collections.unmodifiableMap(arguments);
    }


    protected <T> T getArgument(String argumentName, Class<T> clazz) {
        Object value = arguments.get(argumentName);
        if (clazz.isInstance(value)) {
            return (T) clazz.cast(value);
        }
        if (value != null) {
            // TODO warn that value is of the wrong type?
        }
        return null;
    }

    protected void setArgument(String argumentName, Object value) {
        arguments.put(argumentName, value);
    }


    public static class DescriptorSupport extends StepDescriptor {
        private final String functionName;
        private final String displayName;

        public DescriptorSupport(String functionName, String displayName) {
            this.functionName = functionName;
            this.displayName = displayName;
        }

        @Override
        public String getFunctionName() {
            return functionName;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }
    }

    public static class ExecutionSupport<T> extends SynchronousStepExecution<T> {
        private static final long serialVersionUID = 1L;
        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private transient final StepFunction function;
        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private transient final Map<String, Object> arguments;

        public ExecutionSupport(StepFunction function, Map<String, Object> arguments, StepContext context) {
            super(context);
            this.function = function;
            this.arguments = arguments;
        }

        @Override
        protected T run() throws Exception {
            return invokeFunction();
        }

        protected T invokeFunction() throws Exception {
            return (T) StepHelper.invokeFunction(this.function, this.arguments, getContext());
        }

    }

}