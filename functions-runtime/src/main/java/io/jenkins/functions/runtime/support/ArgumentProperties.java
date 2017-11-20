/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.functions.runtime.support;

import io.jenkins.functions.runtime.ArgumentMetadata;
import io.jenkins.functions.runtime.helpers.PrimitiveTypes;
import io.jenkins.functions.runtime.helpers.Strings;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

/**
 * A helper class for collecting the argument metadata from various places
 * such as properties files or multiple occurrences of the Step annotation on the class or method
 */
public class ArgumentProperties {
    private final String attributeName;
    private String displayName;
    private String description;
    private String typeName;

    public ArgumentProperties(String attributeName) {
        this.attributeName = attributeName;
        this.displayName = attributeName;
    }

    public ArgumentMetadata createAttributeMetadata(ClassLoader classLoader, Class<?> attributeClass) {
        if (Strings.isNullOrEmpty(typeName)) {
            return null;
        }
        String className = Strings.removeGenericsFromClassName(typeName);
        Class<?> clazz = PrimitiveTypes.getClass(className);
        if (clazz == null) {
            try {
                clazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                System.out.println("WARNING: failed to load " + className + " on ClassLoader " + classLoader);
            }
        }
        AnnotatedElement fieldOrParameter = findField(attributeClass, attributeName);
        return new ArgumentMetadata(attributeName, displayName, description, clazz, className, fieldOrParameter);
    }

    private static AnnotatedElement findField(Class<?> type, String name) {
        if (type != null) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }
            Class<?> superclass = type.getSuperclass();
            if (superclass != null && !superclass.equals(Object.class)) {
                return findField(superclass, name);
            }
        }
        return null;
    }

    public void setProperty(String stepName, String propertyName, String value) {
        switch (propertyName) {
            case "description":
                description = value;
                break;
            case "displayName":
                displayName = value;
                break;
            case "type":
                typeName = value;
                break;
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
