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

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Random;

public class XegerUtilsTest {

    @Test
    public void shouldGenerateRandomNumberCorrectly() {
        for (int i = 0; i < 100; i++) {
            Random random = new Random();
            int number = XegerUtils.getRandomInt(3, 7, random);
            Assertions.assertThat(number).isBetween(3, 7);
        }
    }

}
