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

import io.jenkins.functions.Step;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static io.jenkins.functions.loader.helpers.Strings.notEmpty;


/**
 */
public class StepFunctions {

    private static final String STEP_PROPERTIES = "io/jenkins/functions/steps.properties";
    private static final String INVOKE_METHOD = "call";

    public static Map<String, StepFunction> loadStepFunctions(ClassLoader classloader) throws IOException, ClassNotFoundException {
        Map<String, StepFunction> answer = new HashMap<>();
        Enumeration<URL> resources = classloader.getResources(STEP_PROPERTIES);
        if (resources != null) {
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url != null) {
                    loadStepFunctionsForURL(classloader, url, answer);
                }
            }
        }
        
        return answer;
    }

    private static void loadStepFunctionsForURL(ClassLoader classLoader, URL url, Map<String, StepFunction> map) throws IOException, ClassNotFoundException {
        Properties properties = new Properties();
        properties.load(url.openStream());

        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            String name = entry.getKey().toString();
            String className = entry.getValue().toString();
            if (notEmpty(name) && notEmpty(className)) {
                Class<?> clazz = classLoader.loadClass(className);
                map.put(name, createStepFunction(name, clazz, classLoader));
            }
        }

    }

    private static StepFunction createStepFunction(String name, Class<?> clazz, ClassLoader classLoader) {
        Method method;
        try {
            method = clazz.getMethod(INVOKE_METHOD);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Step function class " + clazz.getName() + " does not have a method "
                    + INVOKE_METHOD + " in " + classLoader);
        }

        String description = null;
        Step step = clazz.getAnnotation(Step.class);
        if (step != null) {
            description = step.description();
        }
        Class<?> returnType = method.getReturnType();
        // TODO load argument metadata!
        ArgumentMetadata[] argumentMetadatas = {};
        StepMetadata metadata = new StepMetadata(name, description, returnType, argumentMetadatas);
        return new StepFunctionImpl(name, clazz, method, metadata);
    }
}
