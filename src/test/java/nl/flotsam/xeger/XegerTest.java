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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Originally reported at https://code.google.com/p/xeger/issues/detail?id=13
 */

public class XegerTest {

    private static final String UNION = "|";

    private String ModifyRegex(String regex)
    {
        return regex.replaceAll("\\|", "()|()");
    }
    @Test
    public void testEmpty() {
        String regex = "";
        Xeger xeger = new Xeger(regex);
        String s = xeger.generate();
        assertTrue(s.equals(regex));
        assertTrue("s = " + s + " should match regex", s.matches(regex));
    }

    @Test
    public void testAUnionEmpty() {
        String empty ="";
        String a = "a";
        String regex = a+UNION+empty;
        assertTrue("empty = " + empty + " should match regex", empty.matches(regex));
        assertTrue("a = " + a + " should match regex", a.matches(regex));

        //Next statements impossible since:
        //java.lang.IllegalArgumentException: unexpected end-of-string
        //at dk.brics.automaton.RegExp.next(Unknown Source)
        //Xeger xeger = new Xeger(regex);

        String regexMod = ModifyRegex(regex);
        //System.out.println(regexMod);
        Xeger xegerMod = new Xeger (regexMod);
        String sMod = xegerMod.generate();
        assertTrue(sMod.equals(empty) | sMod.equals(a));
        assertTrue("sMod = " + sMod + " should match regex", sMod.matches(regex));
        assertTrue("sMod = " + sMod + " should match regexMod", sMod.matches(regexMod));
    }

    @Test
    public void testAUnionExplicitEmpty() {
        String empty ="";
        String explicitEmpty = "()";
        String a = "a";
        String regex = a + UNION + explicitEmpty;
        assertTrue("empty = " + empty + " should match regex", empty.matches(regex));
        assertTrue("a = " + a + " should match regex", a.matches(regex));

        Xeger xeger = new Xeger(regex);
        String s = xeger.generate();
        assertTrue(s.equals(empty) | s.equals(a));
        assertTrue("s = " + s + " should match regex", s.matches(regex));
    }

    @Test
    public void testEmptyUnionA() {
        String empty ="";
        String a = "a";
        String regex = empty + UNION + a;

        assertTrue("empty = " + empty + " should match regex", empty.matches(regex));
        assertTrue("a = " + a + " should match regex", a.matches(regex));

        Xeger xeger = new Xeger(regex);
        String s = xeger.generate();
        assertFalse("s = " + s + " unfortunately does not match regex", s.matches(regex));

        String regexMod = ModifyRegex(regex);
        //System.out.println(regexMod);
        Xeger xegerMod = new Xeger (regexMod);
        String sMod = xegerMod.generate();
        assertTrue(sMod.equals(empty) | sMod.equals(a));
        assertTrue("sMod = " + sMod + " should match regex", sMod.matches(regex));
        assertTrue("sMod = " + sMod + " should match regexMod", sMod.matches(regexMod));
    }

    @Test
    public void testExplicitEmptyUnionA() {
        String empty ="";
        String explicitEmpty = "()";
        String a = "a";
        String regex = explicitEmpty + UNION + a;
        assertTrue("empty = " + empty + " should match regex", empty.matches(regex));
        assertTrue("a = " + a + " should match regex", a.matches(regex));

        Xeger xeger = new Xeger(regex);
        String s = xeger.generate();
        assertTrue(s.equals(empty) | s.equals(a));
        assertTrue("s = " + s + " should match regex", s.matches(regex));
    }

    @Test
    public void testSpaceUnionA() {
        String space = " ";
        String a = "a";
        String regex = space+UNION+a;

        assertTrue("space = '" + space + "' should match regex", space.matches(regex));
        assertTrue("a = " + a + " should match regex", a.matches(regex));

        String empty ="";
        assertFalse("empty = " + empty + " should not match regex", empty.matches(regex));

        Xeger xeger = new Xeger(regex);
        String s = xeger.generate();
        assertTrue(s.equals(space) | s.equals(a));
        assertTrue("s = " + s + " should match regex", s.matches(regex));
    }

    @Test
    public void testUnionAEmptyB() {
        String a = "a";
        String empty ="";
        String b = "b";
        String regex = a+UNION+empty+UNION+b;

        assertTrue("a = '" + a + "' should match regex", a.matches(regex));
        assertTrue("empty = '" + empty + "' should match regex", empty.matches(regex));
        assertTrue("b = '" + b + "' should match regex", b.matches(regex));

        Xeger xeger = new Xeger(regex);
        String s = xeger.generate();
        assertTrue("s = '" + s + "' equals 'a' or '|b' ", s.equals(a) | s.equals(UNION+b));

        String regexMod = ModifyRegex(regex);
        //System.out.println(regexMod);
        Xeger xegerMod = new Xeger (regexMod);
        String sMod = xegerMod.generate();
        assertTrue(sMod.equals(a) | sMod.equals(empty) | sMod.equals(b));
        assertTrue("sMod = " + sMod + " should match regex", sMod.matches(regex));
        assertTrue("sMod = " + sMod + " should match regexMod", sMod.matches(regexMod));
    }
}