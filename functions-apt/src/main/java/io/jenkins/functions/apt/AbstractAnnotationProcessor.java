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

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

/**
 * Abstract base class
 */
public abstract class AbstractAnnotationProcessor extends AbstractProcessor {


    protected static String javaTypeName(Element element) {
        TypeMirror typeMirror = element.asType();
        return typeMirror.toString();
    }

    protected void log(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    protected void warning(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    protected void error(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    protected void log(Throwable e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        e.printStackTrace(writer);
        writer.close();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, buffer.toString());
    }

    /**
     * Helper method to produce class output text file using the given handler
     */
    protected void writeFile(String packageName, String fileName, String text) {
        Writer writer = null;
        try {
            Writer out;
            Filer filer = processingEnv.getFiler();
            FileObject resource;
            try {
                resource = filer.getResource(StandardLocation.CLASS_OUTPUT, packageName, fileName);
            } catch (Throwable e) {
                resource = filer.createResource(StandardLocation.CLASS_OUTPUT, packageName, fileName);
            }
            URI uri = resource.toUri();
            File file = null;
            if (uri != null) {
                try {
                    file = new File(uri.getPath());
                } catch (Exception e) {
                    warning("Could not convert output directory resource URI to a file " + e);
                }
            }
            if (file == null) {
                warning("No class output directory could be found!");
            } else {
                file.getParentFile().mkdirs();
                log("Generating file " + file);
                writer = new FileWriter(file);
                writer.write(text);
            }
        } catch (IOException e) {
            log(e);
        } finally {
            io.jenkins.functions.apt.IOHelper.close(writer);
        }
    }

    public Elements getElements() {
        Elements elementUtils = null;
        if (processingEnv != null) {
            elementUtils = processingEnv.getElementUtils();
        }
        return elementUtils;
    }
}
