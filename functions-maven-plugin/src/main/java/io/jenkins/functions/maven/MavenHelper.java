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
package io.jenkins.functions.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class MavenHelper {
    public static URLClassLoader getCompileClassLoader(MavenProject project) {
        try {
            List<String> classpathElements = project.getCompileClasspathElements();
            return createClassLoader(classpathElements, project.getBuild().getOutputDirectory());
        } catch (DependencyResolutionRequiredException e) {
            throw new IllegalArgumentException("Cannot resolve artifact from compile classpath", e);
        }
    }

    private static URLClassLoader createClassLoader(List<String> classpathElements, String... paths) {
        List<URL> urls = new ArrayList<>();
        for (String path : paths) {
            URL url = pathToUrl(path);
            urls.add(url);
        }
        for (Object object : classpathElements) {
            if (object != null) {
                String path = object.toString();
                URL url = pathToUrl(path);
                urls.add(url);
            }
        }
        return createURLClassLoader(urls);
    }


    private static URLClassLoader createURLClassLoader(Collection<URL> jars) {
        return new URLClassLoader(jars.toArray(new URL[jars.size()]));
    }

    private static URL pathToUrl(String path) {
        try {
            File file = new File(path);
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Cannot convert %s to a an URL: %s", path, e.getMessage()), e);
        }
    }

}
