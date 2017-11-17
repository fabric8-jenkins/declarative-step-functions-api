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
package io.jenkins.functions.runtime.support;

import io.jenkins.functions.Logger;
import io.jenkins.functions.runtime.FunctionContext;
import io.jenkins.functions.runtime.StepFunction;
import io.jenkins.functions.runtime.StepMetadata;
import io.jenkins.functions.runtime.helpers.Strings;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 */
public abstract class StepFunctionSupport implements StepFunction {
    private final String name;
    private final Class<?> clazz;
    private final StepMetadata metadata;

    public StepFunctionSupport(String name, Class<?> clazz, StepMetadata metadata) {
        this.name = name;
        this.clazz = clazz;
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        String className = "";
        if (metadata != null) {
            Class<?> clazz = metadata.getImplementationClass();
            if (clazz != null) {
                className = clazz.getName();
            }
        }
        if (Strings.notEmpty(className)) {
            className += "::";
        }
        return getClass().getSimpleName() + "{" + className + getName() + "()}";
    }

    @Override
    public Object invoke(Map<String, Object> arguments, FunctionContext context) {
        Object object = createFunctionObject(context);
        return invokeOnInstance(arguments, context, object);
    }

    @Override
    public Map<String, Object> getArguments(Map<String, Object> arguments, FunctionContext context) {
        Object object = createFunctionObject(context);
        Object allArguments = createArgumentsObject(object, arguments);
        return getAllArguments(allArguments);
    }

    protected Map<String, Object> getAllArguments(Object allArguments) {
        try {
            return PropertyUtils.describe(allArguments);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get properties on bean " + allArguments + ". " + e, e);
        }
    }

    protected Object createFunctionObject(FunctionContext context) {
        Object object;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate class " + clazz.getName() + " due to: " + e, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instantiate class " + clazz.getName() + " due to: " + e, e);
        }
        injectContext(object, context);
        return object;
    }

    protected void injectContext(Object object, FunctionContext context) {
        Logger logger = context.getLogger();
        if (logger != null) {
            setBeanPropertty(object, "logger", logger);
        }
        File currentDir = context.getCurrentDir();
        if (currentDir != null) {
            setBeanPropertty(object, "currentDir", currentDir);
        }
    }

    protected void setBeanPropertty(Object object, String name, Object value) {
        try {
            PropertyUtils.setProperty(object, name, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to set property " + name + " on function object " + object + " due to: " + e, e);
        }
    }

    protected abstract Object invokeOnInstance(Map<String, Object> arguments, FunctionContext context, Object object);

    protected abstract Object createArgumentsObject(Object object, Map<String, Object> arguments);

    @Override
    public StepMetadata getMetadata() {
        return metadata;
    }

    public String getName() {
        return getMetadata().getName();
    }

}
