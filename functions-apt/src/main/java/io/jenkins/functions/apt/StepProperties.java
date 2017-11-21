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
package io.jenkins.functions.apt;

import io.jenkins.functions.Step;

import java.lang.reflect.Method;
import java.util.Properties;

import static io.jenkins.functions.apt.Strings.notEmpty;

/**
 * A helper class for collecting the argument metadata from various places
 * such as properties files or multiple occurrences of the Step annotation on the class or method
 */
public class StepProperties {
    private String name;
    private String displayName;
    private String description;
    private String typeName;

    public StepProperties(String name, String typeName, Step step) {
        this.name = name;
        this.typeName = typeName;
        configure(step);
    }

    public StepProperties(StepProperties parent, String name, Step step) {
        this.name = parent.getName();
        if (notEmpty(name)) {
            this.name = name;
        }
        this.displayName = parent.getDisplayName();
        this.description = parent.getDescription();
        this.typeName = parent.getTypeName();
        configure(step);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        if (Strings.isNullOrEmpty(displayName)) {
            // TODO should we try split camel case?
            return getName();
        }
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getTypeName() {
        return typeName;
    }

    /**
     * Overrides any property with a step annotation; e.g. to override the class level metadata with a method specific annotation
     */
    protected void configure(Step step) {
        if (step != null) {
            String name = step.name();
            String displayName = step.displayName();
            String description = step.description();
            if (notEmpty(name)) {
                this.name = name;
            }
            if (notEmpty(displayName)) {
                this.displayName = displayName;
            }
            if (notEmpty(description)) {
                this.description = description;
            }
        }

    }

    public void store(Properties properties) {
        if (notEmpty(typeName)) {
            properties.put(name + ".typeName", typeName);
        }
        if (notEmpty(displayName)) {
            properties.put(name + ".displayName", displayName);
        }
        if (notEmpty(description)) {
            properties.put(name + ".description", description);
        }
    }
}
