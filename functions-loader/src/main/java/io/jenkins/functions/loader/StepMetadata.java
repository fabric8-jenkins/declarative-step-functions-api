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

import java.util.Arrays;

/**
 */
public class StepMetadata {
    private final String name;
    private final String description;
    private final Class<?> returnType;
    private final ArgumentMetadata[] argumentsMetadata;

    public StepMetadata(String name, String description, Class<?> returnType, ArgumentMetadata[] argumentsMetadata) {
        this.name = name;
        this.description = description;
        this.returnType = returnType;
        this.argumentsMetadata = argumentsMetadata;
    }

    @Override
    public String toString() {
        return "StepMetadata{" + name + Arrays.toString(argumentsMetadata) + " returnType: " + returnType + "}";
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public ArgumentMetadata[] getArgumentsMetadata() {
        return argumentsMetadata;
    }

    /**
     * Returns the method prototype text 
     */
    public String getPrototype() {
        StringBuilder builder = new StringBuilder(name);
        builder.append("(");
        if (argumentsMetadata != null) {
            int count = 0;
            for (ArgumentMetadata parameterInfo : argumentsMetadata) {
                if (count++ > 0) {
                    builder.append(", ");
                }
                builder.append(parameterInfo.getPrototype());
            }
        }
        builder.append(")");
        return builder.toString();
    }

}
