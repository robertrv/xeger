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

import static org.assertj.core.api.Assertions.assertThat;

import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.generator.InRange;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(Theories.class)
public class XegerUtilsTest {

    @Test
    public void testConstructor() {
        assertThat(new XegerUtils()).isNotNull();
    }

    @Theory
    public void shouldGenerateRandomNumberCorrectly(@ForAll int down) {
        int up = down + 10;
        Random random = new Random();
        int number = XegerUtils.getRandomInt(down, up, random);
        assertThat(number).isBetween(down, up);
    }

    @Test
    public void testWithMaxLoops() {
        System.setProperty("nl.flotsam.xeger.MAX_LOOPS", "2");
        String generated = new Xeger("a*").generate();
        assertThat(generated).matches("a*");
        assertThat(generated.length()).isLessThan(2);
    }

    @Test
    public void testWithMaxLoops_InvalidSystemVariable() {
        System.setProperty("nl.flotsam.xeger.MAX_LOOPS", "invalid");
        String generated = new Xeger("a*").generate();
        assertThat(generated).matches("a*");
        assertThat(generated.length()).isLessThan(8);
    }
}
