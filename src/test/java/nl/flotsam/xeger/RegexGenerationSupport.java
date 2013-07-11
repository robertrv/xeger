/**
 * Copyright 2009 Wilfred Springer
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(value = Parameterized.class)
public class RegexGenerationSupport {

    private String regex;
    private boolean working;
    private static final Logger logger = Logger
            .getLogger(RegexGenerationSupport.class.getSimpleName());

    public RegexGenerationSupport(boolean working, String regexToTest) {
        this.regex = regexToTest;
        this.working = working;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{
                // Predefined character classes does not work
                {false, "\\d\\d"},
                {false, "\\d{3}"},
                {false, "\b(\\w+)\\s+\\1\b "},
                // Supported elements from java api
                {true, "[ab]{4,6}c"},
                {true, "a|b"},
                {true, "[abc]"},
                {true, "[^abc]"},
                {true, "a+"},
                {true, "a*"},
                {true, "ab"},
                {true, "[a-zA-Z]"},
                // union and intersection does not works
                {false, "[a-d[m-p]]"},
                {false, "[a-z&&[def]]"},
                {false, "[a-z&&[^bc]]"},
                {false, "[a-z&&[^m-p]]"},
                // predefined character classes
                {true, "."},
                {false, "\\d"},
                {false, "\\D"},
                {false, "\\s"},
                {false, "\\S"},
                {false, "\\w"},
                {false, "\\W"},
                // POSIX character classes
                {false, "\\p{Lower}"},
                {false, "\\p{Upper}"},
                {false, "\\p{ASCII}"},
                {false, "\\p{Alpha}"},
                {false, "\\p{Digit}"},
                {false, "\\p{Alnum}"},
                {false, "\\p{Punct}"},
                {false, "\\p{Graph}"},
                {false, "\\p{Print}"},
                {false, "\\p{Blank}"},
                {false, "\\p{Cntrl}"},
                {false, "\\p{XDigit}"},
                {false, "\\p{Space}"},
                // java.lang.Character classes
                {false, "\\p{javaLowerCase}"},
                {false, "\\p{javaUpperCase}"},
                {false, "\\p{javaWhitespace}"},
                {false, "\\p{javaMirrored}"},
                // Classes for Unicode blocks and categories
                {false, "\\p{InGreek}"},
                {false, "\\p{Lu}"},
                {false, "\\p{Sc}"},
                {false, "\\P{InGreek}"},
                {false, "\\[\\p{L}&&[^\\p{Lu}]]"},
                /*     TODO
// Boundary matchers
{false, "^aaaa"},
{false, "a.*b$"},
{false, "\\b"},
{false, "\\B"},
{false, "\\A"},
{false, "\\G"},
{false, "\\Z"},
{false, "\\z"},


Greedy quantifiers
X?	X, once or not at all
X*	X, zero or more times
X+	X, one or more times
X{n}	X, exactly n times
X{n,}	X, at least n times
X{n,m}	X, at least n but not more than m times

Reluctant quantifiers
X??	X, once or not at all
X*?	X, zero or more times
X+?	X, one or more times
X{n}?	X, exactly n times
X{n,}?	X, at least n times
X{n,m}?	X, at least n but not more than m times

Possessive quantifiers
X?+	X, once or not at all
X*+	X, zero or more times
X++	X, one or more times
X{n}+	X, exactly n times
X{n,}+	X, at least n times
X{n,m}+	X, at least n but not more than m times

Logical operators
XY	X followed by Y
X|Y	Either X or Y
(X)	X, as a capturing group

Back references
\n	Whatever the nth capturing group matched

Quotation
\	Nothing, but quotes the following character
\Q	Nothing, but quotes all characters until \E
\E	Nothing, but ends quoting started by \Q

Special constructs (non-capturing)
(?:X)	X, as a non-capturing group
(?idmsux-idmsux) 	Nothing, but turns match flags i d m s u x on - off
(?idmsux-idmsux:X)  	X, as a non-capturing group with the given flags i d m s u x on - off
(?=X)	X, via zero-width positive lookahead
(?!X)	X, via zero-width negative lookahead
(?<=X)	X, via zero-width positive lookbehind
(?<!X)	X, via zero-width negative lookbehind
(?>X)	X, as an independent, non-capturing group
                 */

                {true, ""},
        };
        return Arrays.asList(data);
    }

    @Test
    public void shouldNotGenerateTextCorrectly() throws Exception {
        try {
            Xeger generator = new Xeger(regex);
            boolean alwaysCorrect = true;
            String regexCleaned = regex.replace("\\", "");
            for (int i = 0; i < 100; i++) {
                String text = generator.generate();

                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO,
                            "For pattern \"{0}\" \t\t, generated: \"{1}\"",
                            new Object[]{regex, text});
                }

                if (working) {
                    assertTrue("text generated: " + text + " does match regexp: "
                            + regex, text.matches(regex));
                } else {
                    /**
                     * In case of non supported we have some which just sometimes works but at some point they doesn't.
                     *
                     * So what we want to check is that not on all the cases the match is correct.
                     */

                    if (text.matches(regex) && !text.equalsIgnoreCase(regexCleaned)) {
                        logger.warning("the current regex worked when shouldn't. Text generated: |" + text
                                + "| does match regexp: |" + regex + "|");
                    } else {
                        alwaysCorrect = false;
                    }
                }
            }

            if (!working) {
                assertFalse("Should has failed some time but always worked ... it is supported! regex: " +
                        regex, alwaysCorrect);
            }
        } catch (Exception maybeIgnored) {
            logger.log(Level.SEVERE, "Error with regex: "+ regex + " which works?: " + working, maybeIgnored);
            if (working) {
                throw maybeIgnored;
            }
        }

    }

}
