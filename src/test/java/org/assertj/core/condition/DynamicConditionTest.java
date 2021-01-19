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

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

public class DynamicConditionTest {

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

    Condition<String> dynamic1 = DynamicCondtion
        .<String, Integer>ofCheck((given, expected) -> given.length() < expected)
        .withExpectedValue(100)
        .withCheckDescription("shorter than")
        .build();

    assertThat(dynamic1.toString()).matches("[NOT EXECUTED] shorter than <100>");
    
    assertThat(dynamic1.matches("foooo")).isTrue();
    System.out.println(dynamic1);
    assertThat(dynamic1.toString()).matches("[OK] shorter than <100>");

    Condition<String> dynamic2 = DynamicCondtion
        .<String, Integer>ofCheck((given, expected) -> given.length() < expected)
        .withExpectedValue(4)
        .withExpectedValueDescription((i) -> i + " (max size)")
        .withCheckedValueDescription((s) -> String.format("%s (original word: %s)", s.length(), s))
        .withCheckDescription("not be longer")
        .build();

    assertThat(dynamic2.matches("foooo")).isTrue();
    System.out.println(dynamic2);
    assertThat(dynamic2.toString())
        .matches("[FAILED] not be longer <4 (max size)> but was <5 (original word: foooo)>");

  }
}
