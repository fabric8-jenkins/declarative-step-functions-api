/**
 * Copyright (C) Original Authors 2017
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.functions.maven;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;

/**
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class GenerateMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * The generation output directory
     */
    @Parameter(property = "jenkins.functions.workDir", defaultValue = "${project.build.directory}/generated-sources")
    private File outputDir;

    /**
     * The generation output directory
     */
    @Parameter(property = "jenkins.functions.workDir", defaultValue = "${project.build.directory}/classes")
    private File jellyOutputDir;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        URLClassLoader classLoader = MavenHelper.getCompileClassLoader(project);
        // TODO allow them to be filtered?
        try {
            Map map = generate(classLoader);

            getLog().info("Generated " + map.size() + " Step classes for the found declarative step functions");

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate step classes due to: " + e, e);
        }

        // TODO should we generate this from the relative folder?
        String generatedSourceRoot = "target/generated-sources";
        if (!project.getCompileSourceRoots().contains(generatedSourceRoot)) {
            project.getCompileSourceRoots().add(generatedSourceRoot);
        }
    }

    protected Map generate(URLClassLoader classLoader) throws MojoExecutionException {
        // lets use class loader to find the generator to avoid class loader complications of multiple
        // APIs and runtimes on the plugin or compile classpaths
        String className = "io.jenkins.functions.runtime.generator.StepAndJellyGenerator";
        Class<?> clazz = null;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Failed to find class " + className + "" +
                    " on the compile classloader to generate the Steps and Jelly. Do you have the functions-runtime jar on the <dependencies>? Got: " + e, e);
        }
        Object generator;
        try {
            generator = clazz.newInstance();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to instantiate class " + clazz.getName() + " due to: " + e, e);
        }
        try {
            PropertyUtils.setProperty(generator, "outputDir", outputDir);
            PropertyUtils.setProperty(generator, "jellyOutputDir", jellyOutputDir);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to configure " + generator + " as a class " + clazz.getName() + " due to: " + e, e);
        }
        Method generateMethod;
        try {
            generateMethod = clazz.getMethod("generate", ClassLoader.class);
        } catch (NoSuchMethodException e) {
            throw new MojoExecutionException("Failed to find generate(ClassLoader) method on class " + clazz.getName() + " due to: " + e, e);
        }
        try {
            Object[] args = { classLoader };
            Object answer = generateMethod.invoke(generator, args);
            if (answer instanceof Map) {
                return (Map) answer;
            } else {
                getLog().warn("Returned " + answer + " which is not a Map!");
                return Collections.emptyMap();
            }
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException("Failed to invoke generator " + e, e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            throw new MojoExecutionException("Failed to invoke generator " + t, t);
        }
    }

}

