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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class StepFunctionsTest {
    protected Map<String, StepFunction> functionMap;

    protected String methodName = "hello";

    public void assertFunctionHasValidMetadata(StepFunction function) {
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
    public void testInvokeFunction() throws Exception {
        StepFunction function = functionMap.get(methodName);
        assertThat(function).describedAs("No function found!").isNotNull();
        assertFunctionHasValidMetadata(function);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", "James");
        Object result = function.invoke(arguments);
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo("Hello James");
    }
}
