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
import io.jenkins.functions.Step;

/**
 */
@Step(name = "helloGoodbye")
public class HelloGoodbyeFunction extends HelloFunction {
    @Argument
    private String bye;

    private String dummyRegularField;

    public HelloGoodbyeFunction() {
    }

    public HelloGoodbyeFunction(String name, String bye) {
        super(name);
        this.bye = bye;
    }

    @Override
    @Step
    public String call() throws Exception {
        return super.call() + " " + bye;
    }

    public String getBye() {
        return bye;
    }

    public void setBye(String bye) {
        this.bye = bye;
    }
}