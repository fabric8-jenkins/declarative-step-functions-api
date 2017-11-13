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
package io.jenkins.functions.sample;

import io.jenkins.functions.Argument;
import io.jenkins.functions.Logger;
import io.jenkins.functions.Result;
import io.jenkins.functions.Step;
import io.jenkins.functions.support.DefaultLogger;

import javax.inject.Inject;
import java.util.function.Function;

@Step(name = "example")
public class ExampleFunction implements Function<ExampleFunction.Context, Result> {

    @Inject
    Logger logger;

    @Override
    public Result apply(Context context) {
        Result result;
        if (logger == null) {
            logger = DefaultLogger.getInstance();
        }
        if (context.message == null) {
            logger.err().println("<message> not provided");
            result = Result.FAILURE;
        } else {
            logger.out().println(String.format("Hello, %s", context.message));
            result = Result.SUCCESS;
        }
        return result;
    }

    public static class Context {
        @Argument
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
