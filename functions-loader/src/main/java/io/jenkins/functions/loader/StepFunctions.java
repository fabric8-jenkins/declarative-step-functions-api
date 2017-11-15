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

import io.jenkins.functions.Step;
import io.jenkins.functions.loader.helpers.Strings;
import io.jenkins.functions.loader.support.ArgumentsStepFunction;
import io.jenkins.functions.loader.support.CallableStepFunction;
import io.jenkins.functions.loader.support.MethodStepFunction;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static io.jenkins.functions.loader.helpers.Strings.notEmpty;

/**
 * Helper functions for invoking functions from the classpath
 */
public class StepFunctions {

    private static final String STEP_PROPERTIES = "io/jenkins/functions/steps.properties";
    private static final String CALL_METHOD = "call";
    private static final String APPLY_METHOD = "apply";

    public static Object invokeFunction(String name, Map<String, Object> arguments, FunctionContext context) throws Exception {
        ClassLoader classLoader = StepFunctions.class.getClassLoader();
        return invokeFunction(name, arguments, context, classLoader);
    }

    public static Object invokeFunction(String name, Map<String, Object> arguments, FunctionContext context, ClassLoader classLoader) throws IOException, ClassNotFoundException, FunctionNotFound {
        Map<String, StepFunction> functions = loadStepFunctions(classLoader);
        return invokeFunction(name, arguments, context, functions);
    }

    public static Object invokeFunction(String name, Map<String, Object> arguments, FunctionContext context, Map<String, StepFunction> functions) throws FunctionNotFound {
        StepFunction function = functions.get(name);
        if (function == null) {
            throw new FunctionNotFound(name);
        }
        return function.invoke(arguments, context);
    }


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
                loadStepFunctionsForClass(name, clazz, classLoader, map);
            }
        }
    }

    private static void loadStepFunctionsForClass(String name, Class<?> clazz, ClassLoader classLoader, Map<String, StepFunction> map) {
        String description = null;
        Step step = clazz.getAnnotation(Step.class);
        if (step != null) {
            description = step.description();
        }

        // TODO load argument metadata!
        ArgumentMetadata[] argumentMetadatas = {};

        Method method;
        try {
            method = clazz.getMethod(CALL_METHOD);
            Class<?> returnType = method.getReturnType();
            StepMetadata metadata = new StepMetadata(name, description, returnType, argumentMetadatas);
            map.put(name, new CallableStepFunction(name, clazz, metadata, method));
        } catch (NoSuchMethodException e) {
            method = findApplyMethod(clazz);
            if (method == null) {
                Map<String, Method> stepMethods = new HashMap<>();
                loadStepMethods(clazz, stepMethods);
                if (stepMethods.isEmpty()) {
                    throw new IllegalArgumentException("Step function class " + clazz.getName() + " does not have a method "
                            + CALL_METHOD + " or " + APPLY_METHOD + " nor has any methods annotated with @Step in " + classLoader);
                } else {
                    Set<Map.Entry<String, Method>> entries = stepMethods.entrySet();
                    for (Map.Entry<String, Method> entry : entries) {
                        String methodName = entry.getKey();
                        method = entry.getValue();
                        Class<?> returnType = method.getReturnType();
                        StepMetadata metadata = new StepMetadata(methodName, description, returnType, argumentMetadatas);
                        map.put(methodName, new MethodStepFunction(name, clazz, metadata, method));
                    }
                }
            } else {
                Class<?> returnType = method.getReturnType();
                StepMetadata metadata = new StepMetadata(name, description, returnType, argumentMetadatas);
                map.put(name, new ArgumentsStepFunction(name, clazz, metadata, method));
            }
        }
    }

    private static void loadStepMethods(Class<?> clazz, Map<String, Method> stepMethods) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                Step step = method.getAnnotation(Step.class);
                if (step != null) {
                    String name = step.name();
                    if (Strings.isNullOrEmpty(name)) {
                        name = method.getName();
                    }
                    if (!stepMethods.containsKey(name)) {
                        stepMethods.put(name, method);
                    }
                }
            }
        }
    }

    private static Method findApplyMethod(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        List<Method> found = new ArrayList<>();
        for (Method method : methods) {
            Step step = method.getAnnotation(Step.class);
            if (method.getName().equals(APPLY_METHOD) && method.getParameterCount() == 1) {
                found.add(method);
            }
        }
        if (found.size() > 1) {
            for (Method method : found) {
                if (!method.getReturnType().equals(Object.class)) {
                    return method;
                }
            }
        }
        if (!found.isEmpty()) {
            return found.get(0);
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && !superclass.equals(clazz)) {
            return findApplyMethod(superclass);
        }
        return null;
    }
}
