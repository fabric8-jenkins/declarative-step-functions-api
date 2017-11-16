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

import io.jenkins.functions.runtime.ArgumentMetadata;
import io.jenkins.functions.runtime.StepFunction;
import io.jenkins.functions.runtime.StepFunctions;
import io.jenkins.functions.runtime.StepMetadata;
import io.jenkins.functions.runtime.helpers.Strings;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;

import static io.jenkins.functions.runtime.helpers.Strings.capitalise;
import static io.jenkins.functions.runtime.helpers.Strings.notEmpty;

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
            Map<String, StepFunction> map = StepFunctions.loadStepFunctions(classLoader);
            for (StepFunction function : map.values()) {
                generateStepClass(function);
            }
            getLog().info("Generated " + map.size() + " Step classes for the found declarative step functions");

            // TODO should we generate this from the relative folder?
            String generatedSourceRoot = "target/generated-sources";
            if (!project.getCompileSourceRoots().contains(generatedSourceRoot)) {
                project.getCompileSourceRoots().add(generatedSourceRoot);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate step classes due to: " + e, e);
        }
    }

    protected void generateStepClass(StepFunction function) throws IOException {
        StepMetadata metadata = function.getMetadata();
        Class<?> implementationClass = metadata.getImplementationClass();
        String packageName = implementationClass.getPackage().getName();
        String name = metadata.getName();
        String displayName = metadata.getDisplayName();
        ArgumentMetadata[] argumentMetadata = metadata.getArgumentMetadata();
        String stepClassName = capitalise(name) + "Step";

        // lets make sure the generated step class doesn't clash with the step implementation class name
        if (implementationClass.getSimpleName().equals(stepClassName)) {
            stepClassName += "Wrapper";
        }

        File jellyDir = jellyOutputDir;
        File packageDir = outputDir;
        String packagePath = "";
        if (notEmpty(packageName)) {
            packagePath = packageName.replace('.', File.separatorChar);
            packageDir = new File(outputDir, packagePath);
            jellyDir = new File(jellyOutputDir, packagePath);
        }
        packageDir.mkdirs();

        generateJelly(new File(jellyDir, stepClassName + File.separator + "config.jelly"), function);


        Imports imports = new Imports();
        imports.addImports("hudson.Extension",
                "io.jenkins.functions.step.StepSupport",
                "io.jenkins.functions.runtime.StepFunction",
                "org.jenkinsci.plugins.workflow.steps.StepContext",
                "org.jenkinsci.plugins.workflow.steps.StepExecution",
                "org.kohsuke.stapler.DataBoundConstructor",
                "org.kohsuke.stapler.DataBoundSetter",
                "java.util.Map");

        StringWriter attributesWriter = new StringWriter();

        if (argumentMetadata != null && argumentMetadata.length > 0) {
            attributesWriter.write("\n    // Argument properties\n\n");

            for (ArgumentMetadata argument : argumentMetadata) {
                String argumentName = argument.getName();
                String propertyName = capitalise(argumentName);
                String typeName = imports.simpleName(argument.getTypeName());
                attributesWriter.write("    public " + typeName + " get" + propertyName + "() {\n" +
                        "        return getArgument(\"" + argumentName + "\", " + Strings.removeGenericsFromClassName(typeName) + ".class);\n" +
                        "    }\n" +
                        "\n" +
                        "    @DataBoundSetter\n" +
                        "    public void set" + propertyName + "(" + typeName + " value) {\n" +
                        "        setArgument(\"" + argumentName + "\", value);\n" +
                        "    }\n" +
                        "\n\n");
            }
        }


        File outputFile = new File(packageDir, stepClassName + ".java");
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            writer.println("/**\n" +
                    " * NOTE DO NOT EDIT THIS FILE!\n" +
                    " *\n" +
                    " * Generated by the functions-maven-plugin\n" +
                    " */");
            if (notEmpty(packageName)) {
                writer.println("package " + packageName + ";");
            }

            Collection<String> importList = imports.getImportList();
            if (!importList.isEmpty()) {
                writer.println();
                for (String importClass : importList) {
                    writer.println("import " + importClass + ";");
                }
            }
            writer.println("\n" +
                    "/**\n" +
                    " * This class exposes the declarative step function as a reusable step in scripted and declarative pipelines\n" +
                    " */\n" +
                    "public class " + stepClassName + " extends StepSupport {\n" +
                    "    public static final String STEP_FUNCTION_NAME = \"" + name + "\";\n" +
                    "    public static final String STEP_DISPLAY_NAME = \"" + displayName + "\";\n" +
                    "\n" +
                    "    @DataBoundConstructor\n" +
                    "    public " + stepClassName + "() {\n" +
                    "        super(STEP_FUNCTION_NAME, " + implementationClass.getSimpleName() + ".class);\n" +
                    "    }");

            writer.print(attributesWriter.toString());

            writer.println("\n" +
                    "    @Override\n" +
                    "    public StepExecution start(StepContext context) throws Exception {\n" +
                    "        return new ExecutionSupport(function(), arguments(), context);\n" +
                    "    }\n" +
                    "\n" +
                    "    @Extension\n" +
                    "    public static class DescriptorImpl extends DescriptorSupport {\n" +
                    "        public DescriptorImpl() {\n" +
                    "            super(STEP_FUNCTION_NAME, STEP_DISPLAY_NAME);\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    public static class Execution extends ExecutionSupport<Void> {\n" +
                    "        public Execution(StepFunction function, Map<String, Object> arguments, StepContext context) {\n" +
                    "            super(function, arguments, context);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n");
        }
    }

    private void generateJelly(File file, StepFunction function) throws IOException {
        StepMetadata metadata = function.getMetadata();
        ArgumentMetadata[] argumentMetadata = metadata.getArgumentMetadata();
        if (argumentMetadata != null && argumentMetadata.length > 0) {
            file.getParentFile().mkdirs();
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
                writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<?jelly escape-by-default='true'?>\n" +
                        "<j:jelly xmlns:j=\"jelly:core\" xmlns:f=\"/lib/form\">");

                for (ArgumentMetadata argMetadata : argumentMetadata) {
                    String name = argMetadata.getName();
                    String displayName = argMetadata.getDisplayName();
                    if (Strings.isNullOrEmpty(displayName)) {
                        displayName = name;
                    }
                    // TODO generate different widgets based on types and annotations / metadata
                    String widget = "<f:textbox/>";
                    writer.println("    <f:entry field=\"" + name + "\" title=\"" + displayName + "\">\n" +
                            "        " + widget + "\n" +
                            "    </f:entry>");
                }
                writer.println("    <f:entry field=\"artifactIdToWaitFor\" title=\"ArtifactId To Wait For\">\n" +
                        "        <f:textbox/>\n" +
                        "    </f:entry>\n" +
                        "</j:jelly>");
            }
        }
    }
}

