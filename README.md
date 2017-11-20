## Jenkins Declarative Step Functions

This library provides a simple way to generate Declarative Pipeline Steps using Java POJOs without users needing to know anything about Jenkins, Pipelines or Jelly.

To use it just create a Java POJO annotating the class and a method with `@Step`. A few different approaches are supported:

### implement java.util.function.Function

 ```java
import io.jenkins.functions.Argument;
import io.jenkins.functions.Step;

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
```

### implement java.util.concurrent.Callable

Or you can injection of the arguments onto the class and use a `Callable`:


```java
import io.jenkins.functions.Argument;
import io.jenkins.functions.Step;

import java.util.concurrent.Callable;

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

### annotate a method with arguments

Or you can just annotation a class with `@Step` and then annotate each method with `@Step` and each parameter with `@Argument` and a name:

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


## Generating a Jenkins Plugin

If you wan to turn your declarative step functions into a Jenkins Plugin so that your step scan be used inside scripted or declarative pipelines then add the following to your `pom.xml`

```xml
  <build>
    <plugins>
      <plugin>
        <groupId>io.jenkins.functions</groupId>
        <artifactId>functions-maven-plugin</artifactId>
        <version>${jenkins-functions.version}</version>
        <executions>
          <execution>
            <id>generate</id>
            <goals>
              <goal>generate</goal>
            </goals>
            <phase>generate-sources</phase>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

This will then automatically generate the Step classes and `config.jelly` files so that your steps appear in the Jenkins Pipeline Syntax and Reference UIs along with working in the Blue Ocean Pipeline Editor.

If you want to see an example plugin using this approach try the [fabric8-declarative-pipeline-step-functions-plugin](https://github.com/fabric8-jenkins/fabric8-declarative-pipeline-step-functions-plugin)

## Invoking a step function from the classpath

```java
Map<String, StepFunction> functions = StepFunctions.loadStepFunctions(getClass().getClassLoader());
StepFunction function = functionMap.get("hello");

Map<String, Object> arguments = new HashMap<>();
arguments.put("name", "James");

Object result = function.invoke(arguments);
```