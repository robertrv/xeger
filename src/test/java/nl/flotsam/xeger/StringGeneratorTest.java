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

/**
 * Originally reported at: https://code.google.com/p/xeger/issues/detail?id=11
 */

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class StringGeneratorTest {
    @Test
    public void testRepeatableRegex() {
        for (int x = 0; x<100; x++) {
            Xeger generator = new Xeger("[ab]{4,6}c", new Random(1000));
            Xeger generator2 = new Xeger("[ab]{4,6}c", new Random(1000));

            List<String> firstRegexList = generateRegex(generator, 100);
            List<String> secondRegexList = generateRegex(generator2, 100);

            for (int i=0; i<firstRegexList.size(); i++) {
                assertEquals("Index mismatch: " + i, firstRegexList.get(i), secondRegexList.get(i));
            }
        }
    }

    private List<String> generateRegex(Xeger generator, int count) {
        List<String> regexList = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            regexList.add(generator.generate());
        }
        return regexList;
    }

    @Test
    public void testGenerateBounded() {
        Xeger generator = new Xeger("ab*", new Random(1000));
        for (int i = 0; i < 100; i++) {
            String generated = generator.generate(3 ,7);
            assertThat(generated.length()).isBetween(3, 7);
        }
    }
}