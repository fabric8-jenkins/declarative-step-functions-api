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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 */
public class FileHelper {
    public static String readFully(File file) throws IOException {
        if (!file.isFile()) {
            return null;
        }
        try (InputStream in = new FileInputStream(file)) {
            return readFully(in);
        }
    }

    public static String readFully(InputStream in) throws IOException {
        try (Reader r = new BufferedReader(new InputStreamReader(in))) {
            return readFully(r);
        }
    }


    public static String readFully(Reader r) throws IOException {
        try (StringWriter w = new StringWriter()) {
            copy(r, w);
            return w.toString();
        }
    }

    public static void copy(Reader reader, Writer writer) throws IOException {
        char[] buffer = new char[8192];
        int len;
        for (; ; ) {
            len = reader.read(buffer);
            if (len > 0) {
                writer.write(buffer, 0, len);
            } else {
                writer.flush();
                break;
            }
        }
    }
}
