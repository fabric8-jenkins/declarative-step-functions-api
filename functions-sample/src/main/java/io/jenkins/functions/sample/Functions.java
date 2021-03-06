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
@Step
public class Functions extends BaseFunction {
    @Step(displayName = "Cheesey hello")
    public String cheese(@Argument(name = "name") String name, @Argument(name = "amount") int amount) {
        return "Hello " + name + " #" + amount;
    }

    @Step(displayName = "Finds the beer")
    public String beer(@Argument(name = "location") String location) {
        return "beer:" + location;
    }
}
