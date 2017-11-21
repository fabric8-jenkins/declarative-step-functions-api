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

import io.jenkins.functions.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 */
public class ProcessHelper {

    public static String runCommandCaptureOutput(File dir, Logger logger, Map<String, String> environmentVariables, String... commands) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(dir);
        applyEnvironmentVariables(builder, environmentVariables);

        StringWriter outputWriter = new StringWriter();
        PrintWriter outputPrintWriter = new PrintWriter(outputWriter);
        int result = doRunCommandAndCaptureOutput(logger, outputPrintWriter, builder, commands);
        outputPrintWriter.close();
        String output = outputWriter.toString();
        if (result != 0) {
            logger.warn("Failed to run commands " + String.join(" ", commands) + " result: " + result);
            logOutput(logger, output, false);
            throw new IOException("Failed to run commands " + String.join(" ", commands) + " result: " + result);
        }
        return output;
    }

    public static int runCommand(File dir, Logger logger, Map<String, String> environmentVariables, File outputFile, File errorFile, String... commands) {
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(dir);
        applyEnvironmentVariables(builder, environmentVariables);
        builder.redirectOutput(outputFile);
        builder.redirectError(errorFile);
        return doRunCommand(logger, builder, commands);
    }

    public static int runCommand(File dir, Logger logger, Map<String, String> environmentVariables, String[] commands) {
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(dir);
        applyEnvironmentVariables(builder, environmentVariables);
        return doRunCommandAndLogOutput(logger, builder, commands);
    }

    protected static void logOutput(Logger log, String output, boolean error) {
        if (Strings.notEmpty(output)) {
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (error) {
                    log.error(line);
                } else {
                    log.info(line);
                }
            }
        }
    }

    protected static void applyEnvironmentVariables(ProcessBuilder builder, Map<String, String> environmentVariables) {
        if (environmentVariables != null) {
            for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
                builder.environment().put(entry.getKey(), entry.getValue());
            }
        }
    }

    protected static int doRunCommand(Logger logger, ProcessBuilder builder, String[] commands) {
        String line = getCommandLine(commands);
        try {
            logger.info("$> " + line);
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.warn("Failed to run command " + line + " in " + builder.directory() + " : exit " + exitCode);
            }
            return exitCode;
        } catch (IOException e) {
            logger.warn("Failed to run command " + line + " in " + builder.directory() + " : error " + e);
        } catch (InterruptedException e) {
            // ignore
        }
        return 1;
    }

    protected static int doRunCommandAndLogOutput(Logger logger, ProcessBuilder builder, String[] commands) {
        String line = getCommandLine(commands);
        try {
            logger.info("$> " + line);
            Process process = builder.start();
            processOutput(process.getInputStream(), logger, false, "output of command: " + line);
            processOutput(process.getErrorStream(), logger, true, "errors of command: " + line);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.warn("Failed to run command " + line + " in " + builder.directory() + " : exit " + exitCode);
            }
            return exitCode;
        } catch (IOException e) {
            logger.warn("Failed to run command " + line + " in " + builder.directory() + " : error " + e);
        } catch (InterruptedException e) {
            // ignore
        }
        return 1;
    }

    protected static int doRunCommandAndCaptureOutput(Logger logger, PrintWriter outputWriter, ProcessBuilder builder, String[] commands) {
        String line = getCommandLine(commands);
        try {
            logger.info("$> " + line);
            Process process = builder.start();

            writeOutput(process.getInputStream(), outputWriter);
            processOutput(process.getErrorStream(), logger, true, "errors of command: " + line);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.warn("Failed to run command " + line + " in " + builder.directory() + " : exit " + exitCode);
            }
            return exitCode;
        } catch (IOException e) {
            logger.warn("Failed to run command " + line + " in " + builder.directory() + " : error " + e);
        } catch (InterruptedException e) {
            // ignore
        }
        return 1;
    }

    protected static String getCommandLine(String[] commands) {
        return Strings.stripPrefix(String.join(" ", commands), "bash -c ");
    }

    protected static void processOutput(InputStream inputStream, Logger logger, boolean error, String description) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                if (error) {
                    logger.error(line);
                } else {
                    logger.info(line);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to process " + description + ": " + e, e);
            throw e;
        }
    }

    protected static void writeOutput(InputStream inputStream, PrintWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                writer.println(line);
            }
        }
    }


}
