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

import io.jenkins.functions.Argument;
import io.jenkins.functions.Step;
import io.jenkins.functions.runtime.helpers.Strings;
import io.jenkins.functions.runtime.support.ArgumentsStepFunction;
import io.jenkins.functions.runtime.support.CallableStepFunction;
import io.jenkins.functions.runtime.support.MethodStepFunction;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static io.jenkins.functions.runtime.helpers.Strings.notEmpty;

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

    protected static void loadStepFunctionsForClass(String name, Class<?> clazz, ClassLoader classLoader, Map<String, StepFunction> map) {
        Step step = clazz.getAnnotation(Step.class);

        Method method;
        try {
            method = clazz.getMethod(CALL_METHOD);
            Class<?> returnType = method.getReturnType();
            ArgumentMetadata[] argumentMetadata = loadArgumentMetadataFromProperties(name, classLoader);
            StepMetadata metadata = new StepMetadata(name, step, returnType, argumentMetadata, clazz);
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
                        ArgumentMetadata[] argumentMetadata = loadArgumentMetadataFromProperties(methodName, classLoader);
                        StepMetadata metadata = new StepMetadata(methodName, step, returnType, argumentMetadata, clazz);
                        map.put(methodName, new MethodStepFunction(name, clazz, metadata, method));
                    }
                }
            } else {
                Class<?> returnType = method.getReturnType();
                ArgumentMetadata[] argumentMetadata = loadArgumentMetadataFromProperties(name, classLoader);
                StepMetadata metadata = new StepMetadata(name, step, returnType, argumentMetadata, clazz);
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

    /**
     * Returns the step function for the given name and implementation class
     */
    public static StepFunction loadFunction(String functionName, Class<?> clazz) {
        Map<String, StepFunction> map = new HashMap<>();
        loadStepFunctionsForClass(functionName, clazz, clazz.getClassLoader(), map);
        StepFunction answer = map.get(functionName);
        if (answer == null) {
            throw new FunctionNotFoundForClass(functionName, clazz);
        }
        return answer;
    }

    protected static ArgumentMetadata[] loadArgumentMetadataFromMethod(Method method) {
        List<ArgumentMetadata> list = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            ArgumentMetadata metadata = ArgumentMetadata.newInstance(parameter);
            if (metadata != null) {
                list.add(metadata);
            }
        }
        return toArgumentMetadataArray(list);
    }

    protected static ArgumentMetadata[] loadArgumentMetadataFromClass(Class<?> clazz) {
        SortedMap<String, ArgumentMetadata> metadataMap = new TreeMap<>();
        Class<?> c = clazz;
        while (true) {
            addArgumentMetadataFromClass(metadataMap, c);
            c = c.getSuperclass();
            if (c == null || c.equals(Object.class)) {
                break;
            }
        }
        return toArgumentMetadataArray(metadataMap.values());
    }

    protected static void addArgumentMetadataFromClass(Map<String, ArgumentMetadata> list, Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            Argument argument = field.getAnnotation(Argument.class);
            if (argument == null) {
                argument = field.getDeclaredAnnotation(Argument.class);
            }
            if (argument != null) {
                ArgumentMetadata metadata = ArgumentMetadata.newInstance(field);
                String name = metadata.getName();
                if (metadata != null && !list.containsKey(name)) {
                    list.put(name, metadata);
                }
            }
        }
    }


    protected static ArgumentMetadata[] loadArgumentMetadataFromProperties(String name, ClassLoader classLoader) {
        Properties properties = new Properties();
        URL resource = classLoader.getResource("io/jenkins/functions/" + name + "-arguments.properties");
        if (resource != null) {
            try {
                properties.load(resource.openStream());
            } catch (IOException e) {
               throw new RuntimeException("Failed to load " + resource + " due to: " + e, e);
            }
        }
        return loadArgumentMetadataFromProperties(name, properties, classLoader);
    }

    protected static ArgumentMetadata[] loadArgumentMetadataFromProperties(String stepName, Properties properties, ClassLoader classLoader) {
        SortedMap<String,ArgumentProperties> map = new TreeMap<>();

        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            Object keyObject = entry.getKey();
            Object valueObject = entry.getValue();
            if (keyObject != null && valueObject != null) {
                String key = keyObject.toString();
                String  value = valueObject.toString();


                int idx = key.lastIndexOf('.');
                if (idx > 0) {
                    String attributeName = key.substring(0, idx);
                    String propertyName = key.substring(idx + 1);
                    ArgumentProperties attribute = map.get(attributeName);
                    if (attribute == null) {
                        attribute = new ArgumentProperties(attributeName);
                        map.put(attributeName, attribute);
                    }
                    attribute.setProperty(stepName, propertyName, value);
                }
            }
        }
        List<ArgumentMetadata> list = new ArrayList<>();
        for (Map.Entry<String, ArgumentProperties> entry : map.entrySet()) {
            ArgumentMetadata metadata = entry.getValue().createAttributeMetadata(classLoader);
            if (metadata != null) {
                list.add(metadata);
            }
        }
        return toArgumentMetadataArray(list);
    }

    protected static class ArgumentProperties {
        private final String attributeName;
        private String displayName;
        private String description;
        private String typeName;

        public ArgumentProperties(String attributeName) {
            this.attributeName = attributeName;
            this.displayName = attributeName;
        }

        public ArgumentMetadata createAttributeMetadata(ClassLoader classLoader) {
            if (Strings.isNullOrEmpty(typeName)) {
                return null;
            }
            Class<?> clazz = null;
            try {
                clazz = classLoader.loadClass(typeName);
            } catch (ClassNotFoundException e) {
                System.out.println("WARNING: failed to load " + typeName + " on ClassLoader " + classLoader);
            }
            return new ArgumentMetadata(attributeName, displayName, description, clazz, typeName);
        }


        public void setProperty(String stepName, String propertyName, String value) {
            switch (propertyName) {
                case "description": description = value; break;
                case "displayName": displayName = value; break;
                case "type": typeName = value; break;
                default:
                    System.out.println("Warning step " + stepName + "  argument " + attributeName + " has unknown property " + propertyName + " with value " + value);
            }
        }


        public String getAttributeName() {
            return attributeName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public String getTypeName() {
            return typeName;
        }

    }
    protected static ArgumentMetadata[] toArgumentMetadataArray(Collection<ArgumentMetadata> list) {
        return list.toArray(new ArgumentMetadata[list.size()]);
    }
}
