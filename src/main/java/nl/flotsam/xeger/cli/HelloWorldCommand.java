/**
 * Copyright 2009 Wilfred Springer
 * Copyright 2012 Jason Pell
 * Copyright 2013 Antonio García-Domínguez
 * Copyright 2013 Roberto Ramírez Vique
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.flotsam.xeger.cli;

import java.util.Collections;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.core.ShellException;

public class HelloWorldCommand implements Command {
    private static final String KEY_NAME = "Name";

    private Command.Descriptor descriptor = null;

    public Descriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new Descriptor() {
                public String getNamespace() {
                    return "xeger";
                }

                public String getName() {
                    return "HelloWorld";
                }

                public String getDescription() {
                    return "Test Hello World";
                }

                public String getUsage() {
                    return "To use just call it! helloWorld";
                }

                public Map<String, String> getArguments() {
                    return Collections.singletonMap(KEY_NAME, "Name of the person who you want to say hello");
                }
            };
        }
        return descriptor;
    }

    public Object execute(Context context) {
        Map<String,Object> argsMap = (Map<String,Object>) context.getValue(Context.KEY_COMMAND_LINE_ARGS);
        if (argsMap == null) {
            throw new IllegalStateException("Something went wrong, we don't have any parameter");
        }
        if (!argsMap.containsKey(KEY_NAME)) {
            throw new ShellException("You should specify ");
        }
        System.out.println("Hello Mr/Mrs " + argsMap.get(KEY_NAME));
        return null; // Why return anything?
    }

    public void plug(Context context) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
