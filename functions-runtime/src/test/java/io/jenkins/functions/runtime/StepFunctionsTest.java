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

import io.jenkins.functions.Result;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class StepFunctionsTest {
    protected Map<String, StepFunction> functionMap;
    protected FunctionContext functionContext = new FunctionContext();

    public void assertFunctionHasValidMetadata(StepFunction function, String methodName) {
        StepMetadata metadata = function.getMetadata();
        assertThat(metadata).describedAs("No metadata on function " + function).isNotNull();
        System.out.println("Function has metadata " + metadata);

        assertThat(metadata.getName()).describedAs("name").isEqualTo(methodName);
        String displayName = metadata.getDisplayName();
        assertThat(displayName).describedAs("display name for " + methodName).isNotEmpty().isNotEqualTo(methodName);
        assertThat(metadata.getReturnType()).isNotNull();
        ArgumentMetadata[] parameterInfos = metadata.getArgumentMetadata();
        assertThat(parameterInfos).
                describedAs("No argument metadata for function " + methodName + " on class " + metadata.getImplementationClass().getName()).
                isNotEmpty();
        for (ArgumentMetadata argumentMetadata : parameterInfos) {
            String argName = argumentMetadata.getName();
            assertThat(argName).describedAs("name of argument for  " + methodName).isNotEmpty();
            assertThat(argumentMetadata.getDisplayName()).describedAs("displayName for " + methodName + "." + argName).isNotEmpty();
        }
        assertThat(parameterInfos).isNotEmpty();
    }

    @Before
    public void init() throws Exception {
        functionMap = StepFunctions.loadStepFunctions(StepFunctionsTest.class.getClassLoader());
        assertThat(functionMap).isNotNull();
    }

    @Test
    public void testInvokeCallableFunction() throws Exception {
        StepFunction function = assertValidFunction("hello");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", "James");
        Object result = function.invoke(arguments, functionContext);
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo("Hello James");
    }

    @Test
    public void testInvokeArgumentsFunction() throws Exception {
        StepFunction function = assertValidFunction("example");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("message", "Hello");
        Object result = function.invoke(arguments, functionContext);
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo(Result.SUCCESS);

        Map<String, Object> defaultArguments = function.getArguments(new HashMap<>(), functionContext);
        assertThat(defaultArguments).describedAs("Default arguments").isNotEmpty();
        assertThat(defaultArguments.get("message")).describedAs("defaultArguments.message").isEqualTo("DefaultMessage");
    }

    @Test
    public void testInvokeAnotherArgumentsFunction() throws Exception {
        StepFunction function = assertValidFunction("anotherFn");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", "James");
        Object result = function.invoke(arguments, functionContext);
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo("Hello James");

        Map<String, Object> defaultArguments = function.getArguments(new HashMap<>(), functionContext);
        assertThat(defaultArguments).describedAs("Default arguments").isNotEmpty();
        assertThat(defaultArguments.get("name")).describedAs("defaultArguments.name").isEqualTo("DefaultName");
    }

    @Test
    public void testInvokeMethodFunction() throws Exception {
        StepFunction function = assertValidFunction("cheese");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", "James");
        arguments.put("amount", 69);
        Object result = function.invoke(arguments, functionContext);
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo("Hello James #69");
    }

    protected StepFunction assertValidFunction(String name) {
        StepFunction function = functionMap.get(name);
        assertThat(function).describedAs("No function found for name: " + name).isNotNull();
        assertFunctionHasValidMetadata(function, name);
        return function;
    }
}
