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

import io.jenkins.functions.runtime.support.StepProperties;

/**
 */
public class StepMetadata {
    private final String name;
    private final String displayName;
    private final String description;
    private final Class<?> returnType;
    private final ArgumentMetadata[] argumentMetadata;
    private final Class<?> implementationClass;

    public StepMetadata(String name, StepProperties step, Class<?> returnType, ArgumentMetadata[] argumentMetadata, Class<?> implementationClass) {
        this.name = name;
        this.returnType = returnType;
        this.argumentMetadata = argumentMetadata;
        this.implementationClass = implementationClass;
        this.displayName = step.getDisplayName();
        this.description = step.getDescription();
    }

    @Override
    public String toString() {
        return "StepMetadata{" + getPrototype() + "}";
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public ArgumentMetadata[] getArgumentMetadata() {
        return argumentMetadata;
    }

    public Class<?> getImplementationClass() {
        return implementationClass;
    }

    /**
     * Returns the method prototype text
     */
    public String getPrototype() {
        StringBuilder builder = new StringBuilder(name);
        builder.append("(");
        if (argumentMetadata != null) {
            int count = 0;
            for (ArgumentMetadata metadata : argumentMetadata) {
                if (count++ > 0) {
                    builder.append(", ");
                }
                builder.append(metadata.getPrototype());
            }
        }
        builder.append(")");
        if (returnType != null) {
            builder.append(" ");
            builder.append(returnType.getName());
        }
        return builder.toString();
    }

}
