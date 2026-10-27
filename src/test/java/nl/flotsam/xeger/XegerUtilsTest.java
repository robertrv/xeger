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

import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class XegerUtilsTest {

    @Test
    public void testConstructor() {
        assertThat(new XegerUtils()).isNotNull();
    }

    @Test
    public void shouldGenerateRandomNumberCorrectly() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int down = random.nextInt(100);
            int up = down + 10;
            int number = XegerUtils.getRandomInt(down, up, random);
            assertThat(number).isBetween(down, up);
        }
    }

    @Test
    public void testWithMaxLoops() {
        System.setProperty("nl.flotsam.xeger.MAX_LOOPS", "2");
        for (int i = 0; i < 100; i++) {
            String generated = new Xeger("a*").generate();
            assertThat(generated).matches("a*");
            assertThat(generated.length()).isLessThan(4); // Because our implementation we will loop
        }
    }

    @Test
    public void testWithMaxLoops_InvalidSystemVariable() {
        System.setProperty("nl.flotsam.xeger.MAX_LOOPS", "invalid");
        String generated = new Xeger("a*").generate();
        assertThat(generated).matches("a*");
        assertThat(generated.length()).isLessThan(8);
    }

    @Test
    public void testMaxGeneratedLength() {
        System.setProperty("nl.flotsam.xeger.MAX_GENERATED_LENGTH", "10");
        for (int i = 0; i < 100; i++) {
            String generated = new Xeger("a*").generate();
            assertThat(generated.length()).isLessThanOrEqualTo(10);
        }
    }

    @Test
    public void testMaxGeneratedLength_InvalidSystemVariable() {
        System.setProperty("nl.flotsam.xeger.MAX_GENERATED_LENGTH", "invalid");
        // Should fall back to default (100) without throwing
        String generated = new Xeger("a*").generate();
        assertThat(generated).matches("a*");
    }

    @Test
    public void testExpandShorthandClasses_digits() {
        assertThat(XegerUtils.expandShorthandClasses("\\d")).isEqualTo("[0-9]");
        assertThat(XegerUtils.expandShorthandClasses("\\D")).isEqualTo("[^0-9]");
        assertThat(XegerUtils.expandShorthandClasses("\\d{3}")).isEqualTo("[0-9]{3}");
    }

    @Test
    public void testExpandShorthandClasses_wordChars() {
        assertThat(XegerUtils.expandShorthandClasses("\\w")).isEqualTo("[a-zA-Z0-9_]");
        assertThat(XegerUtils.expandShorthandClasses("\\W")).isEqualTo("[^a-zA-Z0-9_]");
        assertThat(XegerUtils.expandShorthandClasses("\\w+")).isEqualTo("[a-zA-Z0-9_]+");
    }

    @Test
    public void testExpandShorthandClasses_whitespace() {
        assertThat(XegerUtils.expandShorthandClasses("\\s")).isEqualTo("[ \t\n\r]");
        assertThat(XegerUtils.expandShorthandClasses("\\S")).isEqualTo("[^ \t\n\r]");
    }

    @Test
    public void testExpandShorthandClasses_combined() {
        assertThat(XegerUtils.expandShorthandClasses("\\d\\d\\s\\w")).isEqualTo("[0-9][0-9][ \t\n\r][a-zA-Z0-9_]");
    }

    @Test
    public void testExpandShorthandClasses_noExpansionNeeded() {
        assertThat(XegerUtils.expandShorthandClasses("[a-z]{3}")).isEqualTo("[a-z]{3}");
        assertThat(XegerUtils.expandShorthandClasses("abc")).isEqualTo("abc");
    }

    @Test
    public void testExpandShorthandClasses_insideCharClassUsesBareContents() {
        // Inside [...] the shorthand must expand to bare contents, not a nested bracket group.
        assertThat(XegerUtils.expandShorthandClasses("[\\d.]")).isEqualTo("[0-9.]");
        assertThat(XegerUtils.expandShorthandClasses("[\\w-]")).isEqualTo("[a-zA-Z0-9_-]");
        assertThat(XegerUtils.expandShorthandClasses("[\\s]")).isEqualTo("[ \t\n\r]");
        assertThat(XegerUtils.expandShorthandClasses("[a\\d]")).isEqualTo("[a0-9]");
    }

    @Test
    public void testExpandShorthandClasses_negatedShorthandLeftIntactInsideCharClass() {
        // Negated shorthands have no bare-contents form inside a class; leave them untouched.
        assertThat(XegerUtils.expandShorthandClasses("[\\D]")).isEqualTo("[\\D]");
        assertThat(XegerUtils.expandShorthandClasses("[\\W\\S]")).isEqualTo("[\\W\\S]");
    }

    @Test
    public void testExpandShorthandClasses_outsideClassUnaffectedAfterClass() {
        // The in-class flag must reset at ']' so later shorthands still get the bracketed form.
        assertThat(XegerUtils.expandShorthandClasses("[\\d]\\d")).isEqualTo("[0-9][0-9]");
    }

    @Test
    public void testExpandShorthandClasses_inClassGeneratedStringsMatchPattern() {
        String[] patterns = {"[\\d.]{4}", "[\\w]{5}", "[a\\d]{3}"};
        for (String regex : patterns) {
            Xeger xeger = new Xeger(regex);
            for (int i = 0; i < 50; i++) {
                String generated = xeger.generate();
                assertThat(generated).as("Pattern: " + regex).matches(regex);
            }
        }
    }

    @Test
    public void testExpandShorthandClasses_generatedStringsMatchPattern() {
        String[][] cases = {
            {"\\d{3}", "\\d{3}"},
            {"\\w+",   "\\w+"},
            {"\\d\\s\\w", "\\d\\s\\w"},
        };
        for (String[] pair : cases) {
            String regex = pair[0];
            String javaPattern = pair[1];
            Xeger xeger = new Xeger(regex);
            for (int i = 0; i < 50; i++) {
                String generated = xeger.generate();
                assertThat(generated).as("Pattern: " + regex).matches(javaPattern);
            }
        }
    }
}
