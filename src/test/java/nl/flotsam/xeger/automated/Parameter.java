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
package nl.flotsam.xeger.automated;

public class Parameter {
    public static final int MAX_ITERATIONS = 100;

    private final Boolean works;
    private final String regex;
    private Class<? extends Throwable> expected;
    private Integer iterationsOverride;

    private Parameter(Builder builder) {
        this.works = builder.works;
        this.regex = builder.regex;
        this.expected = builder.expected;
        this.iterationsOverride = builder.iterationsOverride;
    }

    public Boolean getWorks() { return works; }
    public String getRegex() { return regex; }
    public Class<? extends Throwable> getExpected() { return expected; }

    public int iterations() {
        return iterationsOverride != null ? iterationsOverride : MAX_ITERATIONS;
    }

    @Override
    public String toString() {
        return "Parameter(regex=" + regex + ", works=" + works + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean works;
        private String regex;
        private Class<? extends Throwable> expected;
        private Integer iterationsOverride;

        public Builder works(Boolean works) { this.works = works; return this; }
        public Builder regex(String regex) { this.regex = regex; return this; }
        public Builder expected(Class<? extends Throwable> expected) { this.expected = expected; return this; }
        public Builder iterationsOverride(Integer iterationsOverride) { this.iterationsOverride = iterationsOverride; return this; }

        public Parameter build() {
            return new Parameter(this);
        }
    }
}
