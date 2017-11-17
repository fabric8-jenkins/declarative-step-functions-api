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
package io.jenkins.functions.runtime.generator;

import io.jenkins.functions.runtime.helpers.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Maintains a list of import statements and mappings to simple class names so we can try use simple names
 * in generated code where possible but default to fully qualified names if there is a clash of simple names
 *
 * This class also generates the import statements.
 */
public class Imports {
    private Map<String, String> simpleNameToClassName = new HashMap<>();
    private SortedSet<String> importList = new TreeSet<>();

    public void addImports(String... classNames) {
        for (String className : classNames) {
            simpleName(className);
        }
    }

    public SortedSet<String> getImportList() {
        return importList;
    }

    public String simpleName(String className) {
        String genericPart = "";
        String nonGenericClassName = className;
        int genIdx = nonGenericClassName.indexOf('<');
        if (genIdx > 0) {
            genericPart = className.substring(genIdx);
            nonGenericClassName = className.substring(0, genIdx);
        }
        int idx = nonGenericClassName.lastIndexOf('.');
        if (idx > 0) {
            return simpleName(nonGenericClassName, nonGenericClassName.substring(0, idx), nonGenericClassName.substring(idx + 1)) + genericPart;
        } else {
            return className;
        }
    }

    public String simpleName(Class<?> type) {
        return simpleName(type.getName(), type.getPackage().getName(), type.getSimpleName());
    }

    protected String simpleName(String fullName, String packageName, String simpleName) {
        if (Strings.isNullOrEmpty(packageName)) {
            return fullName;
        }
        String current = simpleNameToClassName.get(simpleName);
        if (current == null) {
            simpleNameToClassName.put(simpleName, fullName);
            if (!importList.contains(fullName)) {
                if (fullName.indexOf('.') > 0 && !fullName.startsWith("java.lang")) {
                    importList.add(fullName);
                }
            }
        } else if (!current.equals(fullName)) {
            return fullName;
        }
        return simpleName;
    }
}
