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
