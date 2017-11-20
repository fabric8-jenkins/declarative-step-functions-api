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
package io.jenkins.functions.runtime.helpers;

/**
 * Some String helper methods
 */
public final class Strings {

    private Strings() {
        //Helper class
    }

    /**
     * Returns true if the given text is null or empty string or has <tt>null</tt> as the value
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.length() == 0 || "null".equals(text);
    }

    public static boolean notEmpty(String text) {
        return !isNullOrEmpty(text);
    }

    public static String capitalise(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Returns the type name without the generics postfix
     */
    public static String removeGenericsFromClassName(String typeName) {
        int idx = typeName.indexOf('<');
        if (idx > 0) {
            return typeName.substring(0, idx);
        }
        return typeName;
    }

    /**
     * Splits a CamelCase string using a space between them.
     */
    public static String splitCamelCase(String text) {
        return splitCamelCase(text, " ");
    }

    /**
     * Splits a CamelCase string using a separator string between them.
     */
    public static String splitCamelCase(String text, String separator) {
        StringBuilder buffer = new StringBuilder();
        char last = 'A';
        for (char c : text.toCharArray()) {
            if (Character.isLowerCase(last) && Character.isUpperCase(c)) {
                buffer.append(separator);
            }
            buffer.append(c);
            last = c;
        }
        return buffer.toString();
    }

    public static String humanize(String name) {
        return capitalise(splitCamelCase(name));
    }
}
