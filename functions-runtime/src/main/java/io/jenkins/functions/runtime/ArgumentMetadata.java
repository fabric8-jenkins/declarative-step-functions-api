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

/**
 */
public class ArgumentMetadata {
    private Class<?> type;
    private String name;
    private String description;

    @Override
    public String toString() {
        return "PFuncParameterInfo{" + type + "}";
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

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
