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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertTrue;


@RunWith(value = Parameterized.class)
    public class XegerSimpleTest {

        private String regex;

        public XegerSimpleTest(String regexToTest) {
            this.regex = regexToTest;
        }


        @Parameters
        public static Collection<Object[]> data() {
            Object[][] data = new Object[][] { 
                    { "[ab]{4,6}c" }, 
            };
            return Arrays.asList(data);
        }

        @Test
            public void shouldGenerateTextCorrectly() {
                Xeger generator = new Xeger(regex);
                for (int i = 0; i < 100; i++) {
                    String text = generator.generate();
                    assertTrue("text generated: " + text + 
                            " does match regexp: " +regex, text.matches(regex));
                }
            }

    }
