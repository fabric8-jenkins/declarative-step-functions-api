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
package io.jenkins.functions.loader.support;

import io.jenkins.functions.loader.StepFunction;
import io.jenkins.functions.loader.StepMetadata;

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
    public Object invoke(Map<String, Object> arguments) {
        Object object;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate class " + clazz.getName() + " due to: " + e, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instantiate class " + clazz.getName() + " due to: " + e, e);
        }

        return invokeOnInstance(arguments, object);
    }

    protected abstract Object invokeOnInstance(Map<String, Object> arguments, Object object);

    @Override
    public StepMetadata getMetadata() {
        return metadata;
    }
}
