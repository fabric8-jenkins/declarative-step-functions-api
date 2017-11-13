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
package io.jenkins.functions.sample;

import io.jenkins.functions.Argument;
import io.jenkins.functions.Logger;
import io.jenkins.functions.Result;
import io.jenkins.functions.Step;

import javax.inject.Inject;
import java.util.function.Function;

@Step
public class FunctionExample implements Function<FunctionExample.Context, Result> {

    @Inject
    Logger logger;

    @Override
    public Result apply(Context context) {
        Result result;
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
        public final String message;

        public Context(String message) {
            this.message = message;
        }
    }
}
