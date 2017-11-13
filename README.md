## Jenkins Declarative Step Functions

This library provides a simple way to generate Declarative Pipeline Steps using Java POJOs.

To use it just create a Java POJO which is annotated with `@Step` like this:

```java
@Step(name = "hello")
public class HelloFunction implements Callable<String> {
    @Argument
    private String name;

    @Override
    public String call() throws Exception {
        return "Hello " + name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

Or you can implement a `Function`
 ```java
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
```

Or you can just annotation a class with `@Step` and then annotate each method with `@Step` and each parameter with `@Argument`

```java
@Step
public class Functions {
    @Step
    public String cheese(@Argument(name = "name") String name, @Argument(name = "amount") int amount) {
        return "Hello " + name + " #" + amount;
    }
}
```

The `@Argument` annotation is then used to export any arguments for the step.

Then if you add the `functions-apt` module to your `pom.xml` via:

```xml
  <dependencies>
    <dependency>
      <groupId>io.jenkins.functions</groupId>
      <artifactId>functions-api</artifactId>
    </dependency>
    <dependency>
      <groupId>io.jenkins.functions</groupId>
      <artifactId>functions-apt</artifactId>
      <scope>provided</scope>
    </dependency>
  </dependencies>
```

Then the APT plugin will auto-generate a step file for each function you create. e.g. a file `target/classes/io/jenkins/functions/hello.step` is generated like this:

```groovy
step {
  metadata {
    name 'hello'
  }
  args {
    arg {
      name 'name'
      className 'java.lang.String'
    }
  }
  steps {
    javaStepFunction  'hello ${args}'
  }
}
``` 

These Step functions can then be discovered on the classpath via the `StepFunctions` helper methods as it finds all of the generated `io/jenkins/functions/steps.properties` files on the classpath to then be able to find all the step classes.

## Invoking a step function from the classpath

```java
Map<String, StepFunction> functions = StepFunctions.loadStepFunctions(getClass().getClassLoader());
StepFunction function = functionMap.get("hello");

Map<String, Object> arguments = new HashMap<>();
arguments.put("name", "James");

Object result = function.invoke(arguments);
```