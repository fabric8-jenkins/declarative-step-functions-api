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
import io.jenkins.functions.runtime.helpers.Strings;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 */
public class ArgumentMetadata {
    private final String name;
    private final String displayName;
    private final String description;
    private final Class<?> type;
    private final String typeName;

    public ArgumentMetadata(String name, String displayName, String description, Class<?> type, String typeName) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.typeName = typeName;
    }

    protected static ArgumentMetadata newInstance(Argument argument, AnnotatedElement fieldOrParameter, String defaultName, Class<?> clazz) {
        String name = "";
        String displayName = "";
        String description = "";
        if (argument != null) {
            name = argument.name();
            displayName = argument.displayName();
            description = argument.description();
        }
        if (Strings.isNullOrEmpty(name)) {
            name = defaultName;
        }
        if (Strings.isNullOrEmpty(displayName)) {
            // TODO should we split camelCase?
            displayName = name;
        }
        return new ArgumentMetadata(name, displayName, description, clazz, clazz.getName());
    }

    public static ArgumentMetadata newInstance(Field field) {
        return newInstance(field.getAnnotation(Argument.class), field, field.getName(), field.getType());
    }

    public static ArgumentMetadata newInstance(Parameter parameter) {
        Argument annotation = parameter.getAnnotation(Argument.class);
        if (annotation == null) {
            annotation = parameter.getDeclaredAnnotation(Argument.class);
        }
        return newInstance(annotation, parameter, parameter.getName(), parameter.getType());
    }

    @Override
    public String toString() {
        return "ArgumentMetadata{" + typeName + " " + name + "}";
    }

    /**
     * Returns the method prototype text
     */
    public String getPrototype() {
        StringBuilder builder = new StringBuilder();
        if (type != null) {
            builder.append(type.getName());
        }
        if (name != null && name.length() > 0) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(name);
        }
        return builder.toString();
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

    public Class<?> getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }
}
