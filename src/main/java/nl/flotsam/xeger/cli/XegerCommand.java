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

import nl.flotsam.xeger.CharacterSet;
import nl.flotsam.xeger.Xeger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

@Command(
    name = "xeger",
    description = "Generate random strings that match a given regular expression.",
    mixinStandardHelpOptions = true,
    version = "xeger 0.1"
)
public class XegerCommand implements Callable<Integer> {

    @Parameters(paramLabel = "PATTERN", description = "Regular expression pattern(s) to generate strings for.")
    private List<String> patterns;

    @Option(names = {"-n", "--count"}, description = "Number of strings to generate per pattern (default: 1).")
    private int count = 1;

    @Option(names = {"--min-length"}, description = "Desired minimum length of generated strings (best effort).")
    private int minLength = -1;

    @Option(names = {"--max-length"}, description = "Desired maximum length of generated strings (best effort).")
    private int maxLength = -1;

    @Option(names = {"--seed"}, description = "Random seed for reproducible output.")
    private Long seed;

    @Option(names = {"--charset"}, description = "Restrict generated characters: UNICODE (default), ASCII, PRINTABLE_ASCII.")
    private String charset = "UNICODE";

    public Integer call() {
        CharacterSet characterSet = resolveCharacterSet(charset);
        for (String pattern : patterns) {
            Random random = seed != null ? new Random(seed) : new Random();
            Xeger xeger = new Xeger(pattern, random, characterSet);
            for (int i = 0; i < count; i++) {
                String result = (minLength >= 0 || maxLength >= 0)
                        ? xeger.generate(minLength, maxLength)
                        : xeger.generate();
                System.out.println(result);
            }
        }
        return 0;
    }

    private CharacterSet resolveCharacterSet(String name) {
        switch (name.toUpperCase()) {
            case "ASCII":           return CharacterSet.ASCII;
            case "PRINTABLE_ASCII": return CharacterSet.PRINTABLE_ASCII;
            case "UNICODE":
            default:                return CharacterSet.UNICODE;
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new XegerCommand()).execute(args));
    }
}
