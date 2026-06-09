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

import static org.assertj.core.api.Assertions.assertThat;

public class CharacterSetTest {

    // --- CharacterSet unit tests ---

    @Test
    public void printableAsciiContainsOnlyPrintableChars() {
        CharacterSet cs = CharacterSet.PRINTABLE_ASCII;
        assertThat(cs.contains(' ')).isTrue();
        assertThat(cs.contains('~')).isTrue();
        assertThat(cs.contains('A')).isTrue();
        assertThat(cs.contains('\t')).isFalse();
        assertThat(cs.contains('\n')).isFalse();
        assertThat(cs.contains((char) 0x7F)).isFalse();
    }

    @Test
    public void asciiContainsControlChars() {
        CharacterSet cs = CharacterSet.ASCII;
        assertThat(cs.contains('\t')).isTrue();
        assertThat(cs.contains('\n')).isTrue();
        assertThat(cs.contains((char) 0x7F)).isTrue();
        assertThat(cs.contains((char) 0x80)).isFalse();
    }

    @Test
    public void customRangeWorks() {
        CharacterSet cs = CharacterSet.of('a', 'z');
        assertThat(cs.contains('a')).isTrue();
        assertThat(cs.contains('z')).isTrue();
        assertThat(cs.contains('A')).isFalse();
        assertThat(cs.contains('0')).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void customRangeRejectsInvertedBounds() {
        CharacterSet.of('z', 'a');
    }

    @Test
    public void overlapsDetectsIntersection() {
        CharacterSet cs = CharacterSet.PRINTABLE_ASCII; // ' ' to '~'
        assertThat(cs.overlaps('A', 'Z')).isTrue();
        assertThat(cs.overlaps('\0', '\n')).isFalse();
        assertThat(cs.overlaps('~', (char) 0xFF)).isTrue();
    }

    @Test
    public void clampNarrowsTransitionRange() {
        CharacterSet cs = CharacterSet.PRINTABLE_ASCII; // 0x20..0x7E
        assertThat((int) cs.clampMin((char) 0x00)).isEqualTo(0x20);
        assertThat((int) cs.clampMax((char) 0xFF)).isEqualTo(0x7E);
        assertThat((int) cs.clampMin((char) 0x41)).isEqualTo(0x41); // 'A' unchanged
    }

    // --- Integration tests: Xeger + CharacterSet ---

    @Test
    public void printableAsciiConstraintProducesReadableOutput() {
        Xeger xeger = new Xeger(".", new java.util.Random(42), CharacterSet.PRINTABLE_ASCII);
        for (int i = 0; i < 200; i++) {
            String s = xeger.generate();
            for (char c : s.toCharArray()) {
                assertThat((int) c)
                    .as("Expected printable ASCII (32-126) but got " + (int) c)
                    .isBetween(32, 126);
            }
        }
    }

    @Test
    public void asciiConstraintProducesAsciiOutput() {
        Xeger xeger = new Xeger(".", new java.util.Random(42), CharacterSet.ASCII);
        for (int i = 0; i < 200; i++) {
            String s = xeger.generate();
            for (char c : s.toCharArray()) {
                assertThat((int) c)
                    .as("Expected ASCII (0-127) but got " + (int) c)
                    .isLessThanOrEqualTo(127);
            }
        }
    }

    @Test
    public void patternStillMatchesWithPrintableAsciiConstraint() {
        Xeger xeger = new Xeger("[a-z]{5}", new java.util.Random(42), CharacterSet.PRINTABLE_ASCII);
        for (int i = 0; i < 100; i++) {
            String s = xeger.generate();
            assertThat(s).matches("[a-z]{5}");
        }
    }

    @Test
    public void customCharsetConstrainsOutput() {
        CharacterSet digits = CharacterSet.of('0', '9');
        Xeger xeger = new Xeger("[0-9]+", new java.util.Random(42), digits);
        for (int i = 0; i < 100; i++) {
            String s = xeger.generate();
            assertThat(s).matches("[0-9]+");
        }
    }
}
