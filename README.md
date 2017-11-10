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