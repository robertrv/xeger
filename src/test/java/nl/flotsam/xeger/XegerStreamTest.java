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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class XegerStreamTest {

    @Test
    public void streamIsLazyAndInfinite() {
        Xeger xeger = new Xeger("[a-z]{5}", new Random(42));
        List<String> results = xeger.stream().limit(100).collect(Collectors.toList());
        assertThat(results).hasSize(100);
    }

    @Test
    public void streamValuesMatchPattern() {
        Xeger xeger = new Xeger("[a-z]{5}", new Random(42));
        xeger.stream()
             .limit(200)
             .forEach(s -> assertThat(s).matches("[a-z]{5}"));
    }

    @Test
    public void streamWithBoundsRespectsBestEffortLength() {
        Xeger xeger = new Xeger("ab*", new Random(42));
        xeger.stream(3, 7)
             .limit(100)
             .forEach(s -> assertThat(s.length()).isBetween(3, 7));
    }

    @Test
    public void streamWithSeedIsReproducible() {
        List<String> first  = new Xeger("[a-z]{5}", new Random(1234)).stream().limit(50).collect(Collectors.toList());
        List<String> second = new Xeger("[a-z]{5}", new Random(1234)).stream().limit(50).collect(Collectors.toList());
        assertThat(first).isEqualTo(second);
    }

    @Test
    public void streamWorksWithCharacterSet() {
        Xeger xeger = new Xeger(".", new Random(42), CharacterSet.PRINTABLE_ASCII);
        xeger.stream()
             .limit(200)
             .forEach(s -> {
                 for (char c : s.toCharArray()) {
                     assertThat((int) c).isBetween(32, 126);
                 }
             });
    }

    @Test
    public void streamSupportsParallelCollection() {
        Xeger xeger = new Xeger("[0-9]{4}", new Random(42));
        long count = xeger.stream().limit(500).parallel().filter(s -> s.matches("[0-9]{4}")).count();
        assertThat(count).isEqualTo(500);
    }
}
