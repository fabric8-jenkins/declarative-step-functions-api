/**
 * Copyright (C) Original Authors 2017
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.functions.loader;

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

    public void assertFunctionHasValidMetadata(StepFunction function, String methodName) {
        StepMetadata metadata = function.getMetadata();
        assertThat(metadata).describedAs("No metadata on function " + function).isNotNull();
        System.out.println("Function has metadata " + metadata);

        assertThat(metadata.getName()).isEqualTo(methodName);
        assertThat(metadata.getReturnType()).isNotNull();
        ArgumentMetadata[] parameterInfos = metadata.getArgumentsMetadata();
        assertThat(parameterInfos).isNotNull();

        // TODO
        //assertThat(parameterInfos).isNotEmpty();
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
        Object result = function.invoke(arguments);
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo("Hello James");
    }

    @Test
    public void testInvokeContextFunction() throws Exception {
        StepFunction function = assertValidFunction("example");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("message", "Hello");
        Object result = function.invoke(arguments);
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo(Result.SUCCESS);
    }

    @Test
    public void testInvokeMethodFunction() throws Exception {
        StepFunction function = assertValidFunction("cheese");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", "James");
        arguments.put("amount", 69);
        Object result = function.invoke(arguments);
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
