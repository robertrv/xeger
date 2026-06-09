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

import java.util.Random;
import java.util.List;

/**
 * An object that will generate text from a regular expression. In a way, it's the opposite of a regular expression
 * matcher: an instance of this class will produce text that is guaranteed to match the regular expression passed in.
 */
public class Xeger {

    private final Automaton automaton;
    private final Random random;

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
     * Constructs a new instance, accepting the regular expression and the randomizer.
     *
     * @param regex  The regular expression. (Not <code>null</code>.)
     * @param random The object that will randomize the way the String is generated. (Not <code>null</code>.)
     * @throws IllegalArgumentException If the regular expression is invalid.
     */
    public Xeger(String regex, Random random) {
        assert regex != null;
        assert random != null;
        this.automaton = new RegExp(XegerUtils.expandShorthandClasses(regex)).toAutomaton();
        this.random = random;
    }

    /**
     * As {@link nl.flotsam.xeger.Xeger#Xeger(String, java.util.Random)}, creating a {@link java.util.Random} instance
     * implicityly.
     */
    public Xeger(String regex) {
        this(regex, new Random());
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

            // Populate weightings based on the number of possible characters per transition.
            int[] weightings = new int[transitions.size()];
            int totalWeight = 0;
            for (int i = 0; i < weightings.length; i++) {
                weightings[i] = transitions.get(i).getMax() - transitions.get(i).getMin() + 1;
                totalWeight += weightings[i];
            }

            int option = XegerUtils.getRandomInt(1, totalWeight, random);

            if (current.isAccept() && decideWhetherToStop(builder)) {
                return;
            }

            // Find the transition that corresponds to the chosen random value.
            int discardedWeight = 0;
            int index = -1;
            do {
                discardedWeight += weightings[++index];
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
        char c = (char) XegerUtils.getRandomInt(transition.getMin(), transition.getMax(), random);
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