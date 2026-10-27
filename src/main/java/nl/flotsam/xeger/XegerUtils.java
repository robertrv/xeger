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
     * <p>Expansion is <em>character-class aware</em>. When a positive shorthand
     * appears inside a bracketed character class it expands to its bare contents
     * rather than a bracketed group, so that nesting is not introduced. For
     * example {@code [\d.]} becomes {@code [0-9.]} (correct) rather than
     * {@code [[0-9].]} (nested and wrong). Outside a class the bracketed form is
     * used, e.g. {@code \d} → {@code [0-9]}.
     *
     * <p><strong>Limitation:</strong> the negated shorthands {@code \D},
     * {@code \W} and {@code \S} cannot be represented as bare contents inside a
     * character class (a complement cannot be unioned with other members as a
     * simple range), so they are left unexpanded in that position. Outside a
     * class they expand to the negated bracketed form as before.
     *
     * @param regex The regular expression to process.
     * @return The regex with predefined character classes expanded.
     */
    public static String expandShorthandClasses(String regex) {
        StringBuilder result = new StringBuilder(regex.length());
        int i = 0;
        boolean inCharClass = false;
        while (i < regex.length()) {
            char c = regex.charAt(i);
            if (c == '\\' && i + 1 < regex.length()) {
                char next = regex.charAt(i + 1);
                String expansion = expandShorthand(next, inCharClass);
                if (expansion != null) {
                    result.append(expansion);
                } else {
                    // Not an expandable shorthand in this context — preserve the
                    // backslash and the next char as-is (e.g. \[, \., or a negated
                    // shorthand inside a character class).
                    result.append(c);
                    result.append(next);
                }
                i += 2;
            } else {
                if (c == '[') {
                    inCharClass = true;
                } else if (c == ']') {
                    inCharClass = false;
                }
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }

    /**
     * Returns the replacement for a shorthand letter, or {@code null} if the
     * letter is not an expandable shorthand in the given context.
     *
     * @param letter      The character following a backslash.
     * @param inCharClass Whether the shorthand appears inside a {@code [...]} class.
     */
    private static String expandShorthand(char letter, boolean inCharClass) {
        if (inCharClass) {
            switch (letter) {
                case 'd': return "0-9";
                case 'w': return "a-zA-Z0-9_";
                case 's': return " \t\n\r";
                // Negated shorthands have no bare-contents form inside a class.
                default:  return null;
            }
        }
        switch (letter) {
            case 'd': return "[0-9]";
            case 'D': return "[^0-9]";
            case 'w': return "[a-zA-Z0-9_]";
            case 'W': return "[^a-zA-Z0-9_]";
            case 's': return "[ \t\n\r]";
            case 'S': return "[^ \t\n\r]";
            default:  return null;
        }
    }

}
