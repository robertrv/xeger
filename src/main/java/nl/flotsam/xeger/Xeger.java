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

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.automaton.RegExp;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * An object that will generate text from a regular expression. In a way, it's the opposite of a regular expression
 * matcher: an instance of this class will produce text that is guaranteed to match the regular expression passed in.
 */
public class Xeger {

    private final Automaton automaton;
    private final Random random;
    private final CharacterSet characterSet;

    private long desiredMinLength=-1;
    private long desiredMaxLength=-1;

    /**
     * When traversing cyclic states, after this many iterations the generator will force an escape
     * by preferring a transition that leads to a different state.
     */
    private static final int MAX_LOOPS = 8;

    /**
     * Hard cap on the total number of characters that can be generated in a single {@link #generate()} call.
     * Prevents infinite loops caused by degenerate DFA structures (e.g. patterns with unsupported constructs
     * like boundary matchers that produce all-self-loop states). Configurable via the system property
     * {@code nl.flotsam.xeger.MAX_GENERATED_LENGTH}. Default is 100.
     */
    private static final int MAX_GENERATED_LENGTH = 100;

    /**
     * Constructs a new instance with a specific character set constraint.
     *
     * @param regex        The regular expression. (Not <code>null</code>.)
     * @param random       The randomizer. (Not <code>null</code>.)
     * @param characterSet Restricts generated characters to this set. Use
     *                     {@link CharacterSet#UNICODE} to preserve the original
     *                     unconstrained behaviour. (Not <code>null</code>.)
     * @throws IllegalArgumentException If the regular expression is invalid.
     */
    public Xeger(String regex, Random random, CharacterSet characterSet) {
        assert regex != null;
        assert random != null;
        assert characterSet != null;
        this.automaton = new RegExp(XegerUtils.expandShorthandClasses(regex)).toAutomaton();
        this.random = random;
        this.characterSet = characterSet;
    }

    /**
     * Constructs a new instance using {@link CharacterSet#UNICODE} (default behaviour).
     *
     * @param regex  The regular expression. (Not <code>null</code>.)
     * @param random The randomizer. (Not <code>null</code>.)
     * @throws IllegalArgumentException If the regular expression is invalid.
     */
    public Xeger(String regex, Random random) {
        this(regex, random, CharacterSet.UNICODE);
    }

    /**
     * Constructs a new instance using {@link CharacterSet#UNICODE} and a freshly created {@link java.util.Random}.
     */
    public Xeger(String regex) {
        this(regex, new Random(), CharacterSet.UNICODE);
    }

    /**
     * Generates a random String that is guaranteed to match the regular expression passed to the constructor.
     */
    public String generate() {
        StringBuilder builder = new StringBuilder();
        generate(builder, automaton.getInitialState());
        return builder.toString();
    }

    /**
     * Generates a random String that is guaranteed to match the regular expression passed to the constructor.
     * This version does a best effort of making sure that min and max lengths are adhered to. Note with some
     * regexes this is impossible or unlikely, but it's worth trying.
     * Note -1 means the value is ignored.
     */
    public String generate(int desiredMinLength, int desiredMaxLength) {
        this.desiredMinLength=desiredMinLength;
        this.desiredMaxLength=desiredMaxLength;
        return generate();
    }

    /**
     * Returns an infinite sequential {@link Stream} of random strings, each guaranteed to match
     * the regular expression passed to the constructor.
     *
     * <p>The stream is lazy — strings are generated on demand. Use {@link Stream#limit(long)} to
     * obtain a finite number of results:
     * <pre>
     *     List&lt;String&gt; samples = new Xeger("[a-z]{5}").stream().limit(100).collect(toList());
     * </pre>
     *
     * @return An infinite stream of matching strings.
     */
    public Stream<String> stream() {
        return Stream.generate(this::generate);
    }

    /**
     * Returns an infinite sequential {@link Stream} of random strings with best-effort length
     * bounds applied to each generated value.
     *
     * @param desiredMinLength Minimum desired length (-1 to ignore).
     * @param desiredMaxLength Maximum desired length (-1 to ignore).
     * @return An infinite stream of matching strings.
     */
    public Stream<String> stream(int desiredMinLength, int desiredMaxLength) {
        return Stream.generate(() -> generate(desiredMinLength, desiredMaxLength));
    }

    private void generate(StringBuilder builder, State state) {
        int iterations = 0;
        int maxLoops = getMaxLoops();
        int maxGeneratedLength = getMaxGeneratedLength();
        State current = state;

        while (true) {
            if (builder.length() >= maxGeneratedLength) {
                return;
            }
            List<Transition> transitions = current.getSortedTransitions(false);
            if (transitions.size() == 0) {
                assert current.isAccept();
                return;
            }

            // Populate weightings based on characters available in each transition
            // after intersecting with the configured character set.
            int[] weightings = new int[transitions.size()];
            int totalWeight = 0;
            for (int i = 0; i < weightings.length; i++) {
                Transition t = transitions.get(i);
                if (characterSet.overlaps(t.getMin(), t.getMax())) {
                    char lo = characterSet.clampMin(t.getMin());
                    char hi = characterSet.clampMax(t.getMax());
                    weightings[i] = hi - lo + 1;
                } else {
                    weightings[i] = 0; // transition incompatible with character set
                }
                totalWeight += weightings[i];
            }

            if (totalWeight == 0) {
                // No transition is compatible with the character set; stop if possible,
                // otherwise accept a potentially non-matching result rather than looping.
                return;
            }

            int option = XegerUtils.getRandomInt(1, totalWeight, random);

            if (current.isAccept() && decideWhetherToStop(builder)) {
                return;
            }

            // Find the transition that corresponds to the chosen random value,
            // skipping transitions with zero weight (outside character set).
            int discardedWeight = 0;
            int index = -1;
            do {
                index++;
                discardedWeight += weightings[index];
            } while (discardedWeight < option);

            if (iterations > maxLoops) {
                if (current.isAccept()) {
                    return;
                }
                // We have been looping too long on a non-accept state: force forward progress
                // by preferring a transition that leads to a different (non-current) state.
                index = escapeIndex(transitions, current);
            }

            Transition transition = transitions.get(index);
            appendChoice(builder, transition);
            iterations++;
            current = transition.getDest();
        }
    }

    /**
     * Selects the index of the first transition that leads to a state different from {@code current}.
     * This guarantees forward progress when the traversal is stuck in a cycle on a non-accept state.
     * Falls back to index 0 if every transition is a self-loop (which cannot happen in a valid DFA,
     * since every reachable state must have a path to an accept state).
     */
    private int escapeIndex(List<Transition> transitions, State current) {
        for (int i = 0; i < transitions.size(); i++) {
            if (transitions.get(i).getDest() != current) {
                return i;
            }
        }
        return 0; // fallback: should never be reached for a valid DFA
    }


    private void appendChoice(StringBuilder builder, Transition transition) {
        char lo = characterSet.clampMin(transition.getMin());
        char hi = characterSet.clampMax(transition.getMax());
        char c = (char) XegerUtils.getRandomInt(lo, hi, random);
        builder.append(c);
    }

    private boolean decideWhetherToStop(StringBuilder builder) {
        //Examines the length of the generated string so far (builder.toString()) against
        //the desiredMin and Max values. If min has not been met then returns false (don't stop)
        //if max has been met then return true (do stop) if not then do a random.
        long lengthSoFar = builder.length();
        if (desiredMaxLength > -1 && desiredMaxLength <= lengthSoFar) {
            return true;
        } else if (desiredMinLength > -1 && desiredMinLength > lengthSoFar) {
            return false;
        } else {
            //This is not weighted which means that all values have a 50/50 chance of stopping
            //at each opportunity.
            //For example [A-Z]* will be zero or one characters most of the time.
            return random.nextBoolean();
        }
    }

    private int getMaxLoops() {
        return getIntProperty("nl.flotsam.xeger.MAX_LOOPS", MAX_LOOPS);
    }

    private int getMaxGeneratedLength() {
        return getIntProperty("nl.flotsam.xeger.MAX_GENERATED_LENGTH", MAX_GENERATED_LENGTH);
    }

    private int getIntProperty(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value != null) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException ignored) {
                System.err.println("CAUTION: the value you are using for " + key + " is not a valid integer (" +
                        value + "), now using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

}