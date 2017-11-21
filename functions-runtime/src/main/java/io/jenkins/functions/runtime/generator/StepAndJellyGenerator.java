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
package io.jenkins.functions.runtime.generator;

import io.jenkins.functions.runtime.ArgumentMetadata;
import io.jenkins.functions.runtime.FunctionContext;
import io.jenkins.functions.runtime.StepFunction;
import io.jenkins.functions.runtime.StepFunctions;
import io.jenkins.functions.runtime.StepMetadata;
import io.jenkins.functions.runtime.helpers.Strings;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.jenkins.functions.runtime.helpers.Strings.capitalise;
import static io.jenkins.functions.runtime.helpers.Strings.notEmpty;

/**
 * A generator of Step classes and Jelly code for step functions
 */
public class StepAndJellyGenerator  {
    protected static final Set<String> numberClassNames = new TreeSet<>(Arrays.asList(
            "byte", "short", "int", "long", "float", "double",
            "java.lang.Byte", "java.lang.Short", "java.lang.Int", "java.lang.Long", "java.lang.Float", "java.lang.Double",
            "java.math.BigDecimal", "java.math.BigInteger"
    ));

    private File outputDir = new File(".");
    private File jellyOutputDir = new File(".");


    public StepAndJellyGenerator() {
    }

    public StepAndJellyGenerator(File outputDir, File jellyOutputDir) {
        this.outputDir = outputDir;
        this.jellyOutputDir = jellyOutputDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public File getJellyOutputDir() {
        return jellyOutputDir;
    }

    public void setJellyOutputDir(File jellyOutputDir) {
        this.jellyOutputDir = jellyOutputDir;
    }

    public Map<String, StepFunction> generate(ClassLoader classLoader) throws IOException, ClassNotFoundException {
        Map<String, StepFunction> map = StepFunctions.loadStepFunctions(classLoader);
        for (StepFunction function : map.values()) {
            generateStepClass(function);
        }
        return map;
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

        File jellyDir = this.jellyOutputDir;
        File packageDir = this.outputDir;
        String packagePath = "";
        if (notEmpty(packageName)) {
            packagePath = packageName.replace('.', File.separatorChar);
            packageDir = new File(this.outputDir, packagePath);
            jellyDir = new File(this.jellyOutputDir, packagePath);
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

        StringBuffer constructorParamsWriter = new StringBuffer();
        StringBuffer constructorBodyWriter = new StringBuffer();
        StringWriter attributesWriter = new StringWriter();

        if (argumentMetadata != null && argumentMetadata.length > 0) {
            attributesWriter.write("\n    // Argument properties\n\n");

            for (ArgumentMetadata argument : argumentMetadata) {
                String argumentName = argument.getName();
                String propertyName = capitalise(argumentName);
                String typeName = imports.simpleName(argument.getTypeName());
                String getPrefix = "get";
                if (typeName.equals("boolean")) {
                    getPrefix = "is";
                }

                attributesWriter.write("    public " + typeName + " " + getPrefix + propertyName + "() {\n" +
                        "        return getArgument(\"" + argumentName + "\", " + Strings.removeGenericsFromClassName(typeName) + ".class);\n" +
                        "    }\n" +
                        "\n");

                if (isMandatory(argument)) {
                    if (constructorParamsWriter.length() > 0) {
                        constructorParamsWriter.append(", ");
                    }
                    constructorParamsWriter.append(typeName);
                    constructorParamsWriter.append(" ");
                    constructorParamsWriter.append(argumentName);

                    constructorBodyWriter.append("        setArgument(\"" + argumentName + "\", " + argumentName + ");\n");
                } else {
                            attributesWriter.write("    @DataBoundSetter\n" +
                            "    public void set" + propertyName + "(" + typeName + " value) {\n" +
                            "        setArgument(\"" + argumentName + "\", value);\n" +
                            "    }\n" +
                            "\n\n");
                }
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
                    "    public " + stepClassName + "(" + constructorParamsWriter.toString() + ") {\n" +
                    "        super(STEP_FUNCTION_NAME, " + implementationClass.getSimpleName() + ".class);");
            writer.print(constructorBodyWriter.toString());
            writer.println("    }");

            writer.println(attributesWriter.toString());

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

    protected boolean isMandatory(ArgumentMetadata argument) {
        return argument.getAnnotation(NotNull.class) != null || argument.getAnnotation(NotEmpty.class) != null;
    }

    private void generateJelly(File file, StepFunction function) throws IOException {
        StepMetadata metadata = function.getMetadata();
        List<ArgumentMetadata> sortedArguments = getSortedArguments(metadata);
        if (!sortedArguments.isEmpty()) {
            file.getParentFile().mkdirs();
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
                writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<?jelly escape-by-default='true'?>\n" +
                        "<j:jelly xmlns:j=\"jelly:core\" xmlns:f=\"/lib/form\">");

                HashMap<String, Object> initialArguments = new HashMap<>();
                Map<String,Object> defaultValues;
                try {
                    defaultValues = function.getArguments(initialArguments, new FunctionContext());
                } catch (Exception e) {
                    defaultValues = initialArguments;
                }

                for (ArgumentMetadata argMetadata : sortedArguments) {
                    String name = argMetadata.getName();
                    String defaultExpression = "";
                    Object defaultValue = defaultValues.get(name);
                    if (defaultValue != null && !"".equals(defaultValue)) {
                        // TODO can we refer to the default value rather than hard code it in the Jelly?
                        defaultExpression = " default=\"" + defaultValue + "\"";
                    }

                    String typeName = argMetadata.getTypeName();
                    String displayName = argMetadata.getDisplayName();
                    if (Strings.isNullOrEmpty(displayName)) {
                        displayName = Strings.humanize(name);
                    }
                    List<String> clazzes = new ArrayList<>();
                    if (isMandatory(argMetadata)) {
                        clazzes.add("required");
                    }
                    if (notEmpty(typeName)) {
                        if (isNumber(typeName)) {
                            clazzes.add("number");
                        }
                        // TODO detect other kinds of type like URL / date?
                    }
                    String clazz = "";
                    if (clazzes.size() > 0) {
                        clazz = " clazz=\"" + String.join(" ", clazzes) + "\"";
                    }
                    // generate different widgets based on types and annotations / metadata
                    String widget = "<f:textbox" + defaultExpression + clazz + "/>";
                    if (Strings.notEmpty(typeName)) {
                        String shortTypeName = Strings.removeGenericsFromClassName(typeName);
                        if (shortTypeName.equals("boolean") || shortTypeName.equals("java.lang.Boolean")) {
                            widget = "<f:checkbox" + defaultExpression + "/>";
                        } else {
                            Class<?> type = argMetadata.getType();
                            if (type != null) {
                                if (Iterable.class.isAssignableFrom(type)) {
                                    System.out.println("Ignoring iterable argument for now " + typeName + " for attribute " + name + " on " + metadata.getName());
                                    continue;
                                }
                            }
                        }
                        writer.println("    <f:entry field=\"" + name + "\" title=\"" + displayName + "\">\n" +
                                "        " + widget + "\n" +
                                "    </f:entry>");
                    }
                }
                writer.println("</j:jelly>");
            }
        }
    }

    protected boolean isNumber(String typeName) {
        return numberClassNames.contains(typeName);
    }

    private List<ArgumentMetadata> getSortedArguments(StepMetadata stepMetadata) {
        List<ArgumentMetadata> answer = new ArrayList<>();
        List<ArgumentMetadata> optional = new ArrayList<>();
        ArgumentMetadata[] argumentMetadata = stepMetadata.getArgumentMetadata();
        if (argumentMetadata != null && argumentMetadata.length > 0) {
            for (ArgumentMetadata metadata : argumentMetadata) {
                if (isMandatory(metadata)) {
                    answer.add(metadata);
                } else {
                    optional.add(metadata);
                }
            }
        }
        answer.addAll(optional);
        return answer;
    }
}

