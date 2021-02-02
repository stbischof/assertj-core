/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2021 the original author or authors.
 */
/**
 * 
 */
package org.assertj.core.condition;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

public class VerboseConditionTest {

    /**
     * 
     * <pre>
     * mapped
       using: StringBuilder::toString magic
       from: <StringBuilder> foooo
       to:   <String> foooo
       then checked: [
          all of:[
       [NOT EXECUTED]shorter than 100,
       [NOT EXECUTED]not be longer 4 (max size),
       any of:[
          [NOT EXECUTED]shorter than 100,
          [NOT EXECUTED]not be longer 4 (max size),
          all of:[
             [NOT EXECUTED]shorter than 100,
             [NOT EXECUTED]not be longer 4 (max size)
          ]
       ],
       [NOT EXECUTED]shorter than 100
    ]
    ]
    [OK] shorter than <100>
    [FAILED] not be longer <4 (max size)> but was <5 (original word: foooo)>
     * </pre>
     */
    @Test
    public static void Constructor_test() {

        Condition<String> verboseCondition1 = verbose1();

        assertThat(verboseCondition1.toString()).matches("[ ] shorter than <100>");

        assertThat(verboseCondition1.matches("foooo")).isTrue();
        System.out.println(verboseCondition1);
        assertThat(verboseCondition1.toString()).matches("[✓] shorter than <100>");

        Condition<String> verboseCondition2 = VerboseCondition.verbose(4,
                (String given, Integer expected) -> given.length() < expected, "not be longer",
                (i) -> i + " (max size)", (s) -> String.format("%s (original word: %s)", s.length(), s));

        assertThat(verboseCondition2.matches("foooo")).isTrue();
        System.out.println(verboseCondition2);
        assertThat(verboseCondition2.toString())
                .matches("[✗] not be longer <4 (max size)> but was <5 (original word: foooo)>");
    }

    @Test
    public static void output() {
        Condition<String> c = AnyOf.anyOf(verbose1(), verbose2(), AnyOf.anyOf(verbose1(), verbose2()),
                AllOf.allOf(verbose1(), verbose2()));
        c.matches("foooo");
        
        System.out.println(c);
    }

    private static Condition<String> verbose2() {
        Condition<String> verboseCondition2 = VerboseCondition.verbose(4,
                (String given, Integer expected) -> given.length() < expected, "not be longer",
                (i) -> i + " (max size)", (s) -> String.format("%s (original word: %s)", s.length(), s));
        return verboseCondition2;
    }

    private static Condition<String> verbose1() {
        Condition<String> verboseCondition1 = VerboseCondition.verbose(100,
                (String given, Integer expected) -> given.length() < expected, "shorter than");
        return verboseCondition1;
    }
}
