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
package nl.flotsam.xeger;

import java.util.Random;

/**
 * Utility methods for the Xeger library.
 */
public class XegerUtils {

    /**
     * Generates a random number within the given bounds.
     *
     * @param min    The minimum number (inclusive).
     * @param max    The maximum number (inclusive).
     * @param random The object used as the randomizer.
     * @return A random number in the given range.
     */
    public final static int getRandomInt(int min, int max, Random random) {
        // Uses random.nextInt to guarantee a uniform distribution
        int maxForRandom = max - min + 1;
        return random.nextInt(maxForRandom) + min;
    }

    /**
     * Expands Java predefined character class shorthand sequences into their
     * brics-automaton-compatible equivalents before the regex is compiled.
     *
     * <p>Supported expansions:
     * <ul>
     *   <li>{@code \d} → {@code [0-9]}</li>
     *   <li>{@code \D} → {@code [^0-9]}</li>
     *   <li>{@code \w} → {@code [a-zA-Z0-9_]}</li>
     *   <li>{@code \W} → {@code [^a-zA-Z0-9_]}</li>
     *   <li>{@code \s} → {@code [ \t\n\r\f]}</li>
     *   <li>{@code \S} → {@code [^ \t\n\r\f]}</li>
     * </ul>
     *
     * <p>Escaped backslashes ({@code \\d}, {@code \\w}, {@code \\s}, etc.) are
     * left untouched so that literal backslash + letter sequences are not
     * incorrectly expanded.
     *
     * @param regex The regular expression to process.
     * @return The regex with predefined character classes expanded.
     */
    public static String expandShorthandClasses(String regex) {
        StringBuilder result = new StringBuilder(regex.length());
        int i = 0;
        while (i < regex.length()) {
            char c = regex.charAt(i);
            if (c == '\\' && i + 1 < regex.length()) {
                char next = regex.charAt(i + 1);
                switch (next) {
                    case 'd': result.append("[0-9]");           i += 2; break;
                    case 'D': result.append("[^0-9]");          i += 2; break;
                    case 'w': result.append("[a-zA-Z0-9_]");    i += 2; break;
                    case 'W': result.append("[^a-zA-Z0-9_]");   i += 2; break;
                    case 's': result.append("[ \t\n\r]");  i += 2; break;
                    case 'S': result.append("[^ \t\n\r]"); i += 2; break;
                    default:
                        // Not a shorthand — preserve the backslash and the next char as-is
                        result.append(c);
                        result.append(next);
                        i += 2;
                        break;
                }
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }

}
