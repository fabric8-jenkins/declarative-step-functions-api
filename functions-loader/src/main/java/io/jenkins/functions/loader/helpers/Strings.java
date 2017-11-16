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
package io.jenkins.functions.loader.helpers;

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
}
