package nl.flotsam.xeger;

/**
 * Originally reported at: https://code.google.com/p/xeger/issues/detail?id=11
 */

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class StringGeneratorTest {
    @Test
    public void testRepeatableRegex() {
        for (int x = 0; x<1000; x++) {
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
}