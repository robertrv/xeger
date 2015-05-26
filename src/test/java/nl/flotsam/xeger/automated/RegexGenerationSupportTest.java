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
package nl.flotsam.xeger.automated;

import nl.flotsam.xeger.Xeger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
public class RegexGenerationSupportTest {

    private static final Logger LOGGER = Logger.getLogger(RegexGenerationSupportTest.class.getSimpleName());

    private final Parameter parameter;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    public RegexGenerationSupportTest(Parameter parameter) {
        this.parameter = parameter;
    }

    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{
                {Parameter.builder().works(true).regex("").build()},
                // Predefined character classes does not work
                {Parameter.builder().works(false).regex("\\d\\d").build()},
                {Parameter.builder().works(false).regex("\\d{3}").build()},
                {Parameter.builder().works(false).regex("\b(\\w+)\\s+\\1\b ").build()},
                // Supported elements from java api
                {Parameter.builder().works(true).regex("[ab]{4,6}c").build()},
                {Parameter.builder().works(true).regex("a|b").build()},
                {Parameter.builder().works(true).regex("[abc]").build()},
                {Parameter.builder().works(true).regex("[^abc]").build()},
                {Parameter.builder().works(true).regex("a+").build()},
                {Parameter.builder().works(true).regex("a*").build()},
                {Parameter.builder().works(true).regex("ab").build()},
                {Parameter.builder().works(true).regex("[a-zA-Z]").build()},
                // union and intersection does not works
                {Parameter.builder().works(false).regex("[a-d[m-p]]").build()},
                {Parameter.builder().works(false).regex("[a-z&&[def]]").build()},
                {Parameter.builder().works(false).regex("[a-z&&[^bc]]").build()},
                {Parameter.builder().works(false).regex("[a-z&&[^m-p]]").build()},
                {Parameter.builder().works(false).regex("a||b").build()},
                {Parameter.builder().works(false).regex("a|").build()},
                {Parameter.builder().works(false).regex("|b").build()},
                // predefined character classes
                {Parameter.builder().works(true).regex(".").build()},
                {Parameter.builder().works(false).regex("\\d").build()},
                {Parameter.builder().works(false).regex("\\D").build()},
                {Parameter.builder().works(false).regex("\\s").build()},
                {Parameter.builder().works(false).regex("\\S").build()},
                {Parameter.builder().works(false).regex("\\w").build()},
                {Parameter.builder().works(false).regex("\\W").build()},
                // POSIX character classes
                {Parameter.builder().works(false).regex("\\p{Lower}").build()},
                {Parameter.builder().works(false).regex("\\p{Upper}").build()},
                {Parameter.builder().works(false).regex("\\p{ASCII}").build()},
                {Parameter.builder().works(false).regex("\\p{Alpha}").build()},
                {Parameter.builder().works(false).regex("\\p{Digit}").build()},
                {Parameter.builder().works(false).regex("\\p{Alnum}").build()},
                {Parameter.builder().works(false).regex("\\p{Punct}").build()},
                {Parameter.builder().works(false).regex("\\p{Graph}").build()},
                {Parameter.builder().works(false).regex("\\p{Print}").build()},
                {Parameter.builder().works(false).regex("\\p{Blank}").build()},
                {Parameter.builder().works(false).regex("\\p{Cntrl}").build()},
                {Parameter.builder().works(false).regex("\\p{XDigit}").build()},
                {Parameter.builder().works(false).regex("\\p{Space}").build()},
                // java.lang.Character classes
                {Parameter.builder().works(false).regex("\\p{javaLowerCase}").build()},
                {Parameter.builder().works(false).regex("\\p{javaUpperCase}").build()},
                {Parameter.builder().works(false).regex("\\p{javaWhitespace}").build()},
                {Parameter.builder().works(false).regex("\\p{javaMirrored}").build()},
                // Classes for Unicode blocks and categories
                {Parameter.builder().works(false).regex("\\p{InGreek}").build()},
                {Parameter.builder().works(false).regex("\\p{Lu}").build()},
                {Parameter.builder().works(false).regex("\\p{Sc}").build()},
                {Parameter.builder().works(false).regex("\\P{InGreek}").build()},
                {Parameter.builder().works(false).regex("\\[\\p{L}&&[^\\p{Lu}]]").build()},
                // Boundary matchers
                {Parameter.builder().works(false).regex("^aaaa").build()},
                {Parameter.builder().works(false).regex("^abc$").build()},
                {Parameter.builder().works(false).regex("a.*b$").build()},
                {Parameter.builder().works(false).regex("\\b").build()},
                {Parameter.builder().works(false).regex("\\B").build()},
                {Parameter.builder().works(false).regex("\\A").build()},
                {Parameter.builder().works(false).regex("\\G").build()},
                {Parameter.builder().works(false).regex("\\Z").build()},
                {Parameter.builder().works(false).regex("\\z").build()},
                // Optionals
                {Parameter.builder().works(true).regex("A?BC").build()},
                {Parameter.builder().works(true).regex("AB?C").build()},
                {Parameter.builder().works(true).regex("ABC?").build()},

                // Greedy quantifiers
                {Parameter.builder().works(false).regex("A*BC").iterationsOverride(10000).expected(StackOverflowError.class).build()},
                {Parameter.builder().works(false).regex("AB*C").iterationsOverride(10000).expected(StackOverflowError.class).build()},
                {Parameter.builder().works(true).regex("ABC*").build()},

                {Parameter.builder().works(false).regex("A+BC").iterationsOverride(1000).expected(StackOverflowError.class).build()},
                {Parameter.builder().works(false).regex("AB+C").iterationsOverride(1000).expected(StackOverflowError.class).build()},
                {Parameter.builder().works(true).regex("ABC+").build()},
                {Parameter.builder().works(true).regex("A{3}BC").build()},
                {Parameter.builder().works(true).regex("AB{3}C").build()},
                {Parameter.builder().works(true).regex("ABC{3}").build()},
                {Parameter.builder().works(true).regex("A{0}BC").build()},
                {Parameter.builder().works(true).regex("AB{0}C").build()},
                {Parameter.builder().works(true).regex("ABC{0}").build()},
                {Parameter.builder().works(true).regex("A{2}BC{2}").build()},
                {Parameter.builder().works(true).regex("X{2,}").build()},
                {Parameter.builder().works(true).regex("X{1,5}").build()},

                // Reluctant quantifiers
                {Parameter.builder().works(true).regex("X{1,5}").build()},
                {Parameter.builder().works(true).regex("X??").build()},
                {Parameter.builder().works(true).regex("X*?").build()},
                {Parameter.builder().works(false).regex("X+?").build()},
                {Parameter.builder().works(false).regex("X{2}?").build()},
                {Parameter.builder().works(false).regex("X{2,}?").build()},
                {Parameter.builder().works(false).regex("X{2,5}?").build()},

                //Possessive quantifiers
                {Parameter.builder().works(false).regex("X?+").build()},
                {Parameter.builder().works(true).regex("X*+").build()},
                {Parameter.builder().works(true).regex("X++").build()},
                {Parameter.builder().works(false).regex("X{2}+").build()},
                {Parameter.builder().works(true).regex("X{0}+").build()},
                {Parameter.builder().works(true).regex("X{2,}+").build()},
                {Parameter.builder().works(true).regex("X{0,}+").build()},
                {Parameter.builder().works(false).regex("X{1,2}+").build()},
                {Parameter.builder().works(false).regex("X{0,2}+").build()},

                //Logical operators
                {Parameter.builder().works(true).regex("XY").build()},
                {Parameter.builder().works(true).regex("X|Y").build()},
                {Parameter.builder().works(true).regex("(X)").build()},

            };
        return Arrays.asList(data);
    }

    @Test
    public void testBasedOnParameters() throws Exception {
        if (parameter.getExpected() != null) {
            exceptionRule.expect(parameter.getExpected());
        }
        try {
            Xeger generator = new Xeger(parameter.getRegex());
            boolean alwaysCorrect = true;
            String regexCleaned = parameter.getRegex().replace("\\", "");
            for (int i = 0; i < parameter.iterations(); i++) {
                String text = generator.generate();

                LOGGER.log(Level.INFO,
                        "{0}:For pattern \"{1}\" \t\t, generated: \"{2}\"",
                        new Object[]{i, parameter.getRegex(), text});

                if (parameter.getWorks()) {
                    assertTrue("text generated: " + text + " does match regexp: "
                            + parameter.getRegex(), text.matches(parameter.getRegex()));
                } else {
                    /**
                     * In case of non supported we have some which just sometimes works but at some point they doesn't.
                     *
                     * So what we want to check is that not on all the cases the match is correct.
                     */

                    if (text.matches(parameter.getRegex()) && !text.equalsIgnoreCase(regexCleaned)) {
                        LOGGER.warning("the current regex worked when shouldn't. Text generated: |" + text
                                + "| does match regexp: |" + parameter.getRegex() + "|");
                    } else {
                        alwaysCorrect = false;
                    }
                }
            }

            if (!parameter.getWorks()) {
                assertFalse("Should has failed some time but always worked ... it is supported! regex: " +
                        parameter.getRegex(), alwaysCorrect);
            }
        } catch (Exception maybeIgnored) {
            LOGGER.log(Level.SEVERE, "Error with regex: " + parameter.getRegex() + " which works?: " + parameter.getWorks(), maybeIgnored);
            if (parameter.getWorks()) {
                throw maybeIgnored;
            }
        }

    }
}