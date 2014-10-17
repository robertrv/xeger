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
     * Constructs a new instance, accepting the regular expression and the randomizer.
     *
     * @param regex  The regular expression. (Not <code>null</code>.)
     * @param random The object that will randomize the way the String is generated. (Not <code>null</code>.)
     * @throws IllegalArgumentException If the regular expression is invalid.
     */
    public Xeger(String regex, Random random) {
        assert regex != null;
        assert random != null;
        this.automaton = new RegExp(regex).toAutomaton();
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
        List<Transition> transitions = state.getSortedTransitions(false);
        if (transitions.size() == 0) {
            assert state.isAccept();
            return;
        }
        
        //Try to ascertain the value of the transitions
        int weightings[] = new int[transitions.size()];
        int totalWeight;

        //Populates and array called weightings which looks at the number of possible
        //characters in that transition. The random select is then weighted.
        totalWeight=0;
        for (int i=0; i<weightings.length; i++) {
            weightings[i] = transitions.get(i).getMax()-transitions.get(i).getMin() + 1;
            totalWeight+=weightings[i];
        }

        int option = XegerUtils.getRandomInt(1, totalWeight, random);
        
        if (state.isAccept() && decideWhetherToStop(builder)) {
            return;
        }
        
        //Loop to test the random number against the weightings array.
        int discardedWeight = 0;
        int index=-1;
        do {
            discardedWeight+=weightings[++index];
        } while (discardedWeight < option);
        
        // Moving on to next transition
        Transition transition = transitions.get(index);
        appendChoice(builder, transition);
        generate(builder, transition.getDest());
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

}