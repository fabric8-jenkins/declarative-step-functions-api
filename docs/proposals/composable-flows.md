# Composable Flows

Typical CI / CD pipelines are fairly complex with lots of nested steps being used to implement them. 

Having to explicitly author each small step by hand in a `Jenkinsfile` is sub-optimal and will lead to lots of copy-paste, lack of reuse and a maintenance nightmare.

e.g. a typical [Fabric8 pipeline](https://github.com/fabric8-jenkins/fabric8-declarative-pipeline-step-functions) consists of these kinds of steps...

```yaml
  mavenPipeline:
    ciPipeline:
    cdPipeline:
      stageProject:
      releaseProject:
        promoteImages:
        tagImages:
        waitUntilPullRequestMerged:
        waitUntilArtifactSyncedWithCentral:
      
```  

For most apps a single top level `mavenPipeline` step will do the trick with a few properties to configure. 

```groovy
pipeline {
  agent any
  stages {
    stage('Maven Release') {
      steps {
        mavenPipeline(gitCloneUrl: 'foo.git') {
        }
      }
    }
  }
}
```

However developers may wish to do things like

* disable a nested block/step (e.g. `promoteImages` or `waitUntilArtifactSyncedWithCentral`)
* add overloaded configuration of a nested block/step
* add pre or post steps to a nested block/step

## Idea

Lets introduce a class, say, `StepCustomisation` which is a configurable Argument that lets a Step developer provide an extension point to let users disable, override, configure or add pre/post hooks.

e.g. imagine this tree of step functions:

```yaml
a:
 b:
  d:
 c:
```

Then the implementation of A could be something like:
```yaml
import io.jenkins.functions.Argument;
import io.jenkins.functions.Step;

import java.util.function.Function;

@Step(name = "a")
public class A extends FunctionSupport implements Function<A.Arguments, Result> {

    @Override
    public String apply(Arguments args) {
      ...
      // invoke B
      arguments.getCallB().invoke(new B(this), args.createBArguments());
      
      // invoke C
      arguments.getCallC().invoke(new C(this), args.createCArguments());
            
      return null;        
    }

    public static class Arguments {
      private StepCustomization<B,String> callB = new StepCustomization<>();
      private StepCustomization<C,String> callC = new StepCustomization<>();
      
      ... 
      
      public StepCustomization<B,String> getCallB() { 
        return callB;
      }
      
      public B.Arguments createBArguments() { ... }
    }
}
```

Then the `StepCustomization` for `B and C` can be configured in the usual declarative pipeline way to disable the execution of the step; to add custom configuration, to replace the implementation or to add pre/post steps.

Then the Blue Ocean Pipeline Editor could visualse B and C in the pipeline tree below A so that a user can click on B and add pre/post steps or replacement steps or disable it.

This could then generate a declarative pipeline something like the following (assuming that the user wishes to configure/customise `B, C and D` - in practice they only may need to configure one of those):
```groovy
pipeline {
  agent any
  stages {
    stage('Maven Release') {
      steps {
        mavenPipeline {
          b {
            pre {
              sh "echo pre-B"
            }
            post {
              sh "echo post-B"
            }
            
            children {
              d {
                pre {
                  sh "echo pre-D"
                } 
              }
            }
          }
          c {
            steps {
              sh "echo this is a replacement for the entire C step function"
            }
          }
        }
      }
    }
  }
}
```

## Changes in Blue Ocean Pipeline Editor

We would need the Step/Descriptor to expose the `StepCustomisation` POJOs so that the Blue Ocean UI can:

* render a Step with nested StepCustomisations as a tree (rather than a single step). e.g. adding `A` would render as:

```yaml
a:
 b:
  d:
 c:
```

* each child `StepCustomization` node allows the user to add new steps at the `pre, post` or `steps` areas for that point in the tree. e.g. add a `pre/post/steps` step for `B, C or D`
* the tree structure is essentially immutable; however if a user clicks on, say, B and does a Delete then it configures the `StepCustomisation` bean for `B` to disable all of its steps.

## Summary

This proposal adds the ability to create complex reusable flows which are usable as simple atomic steps in the Blue Ocean pipeline editor and keeps your `Jenkinsfile` very concise, DRY and easy to maintain - but also lets you override and extend / customise any step in the entire tree of the flow.