/*
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
 * Copyright 2012-2020 the original author or authors.
 */
package org.assertj.core.condition;

import java.util.function.BiPredicate;
import java.util.function.Function;

import org.assertj.core.api.Condition;

/**
 * VerboseCondition that shows the expected and the tested value in the
 * description.
 */
public final class VerboseCondition<T, EXPECTED> extends Condition<T> {

  private EXPECTED expectedValue;

  private BiPredicate<T, EXPECTED> matchesBiPredicate;

  private String matchDescription;

  private Function<T, ?> givenValueTransformation;

  private Function<EXPECTED, ?> expectedTransformation;


  private enum State {
    NOT_EXECUTED, OK, FAILED
  }

  public static Function<State, String> defaultState() {

    return state -> {
      switch (state) {
        case NOT_EXECUTED:
          return "[ ]";
        case OK:
          return "[✓] ";
        case FAILED:
          return "[✗] ";
        default:
          return "[?] ";
      }
    };

  }

  public static <T,EXPECTED> VerboseCondition<T,EXPECTED> descriptive(EXPECTED expectedValue, BiPredicate<T, EXPECTED> matchesBiPredicate) {
	  return new VerboseCondition<T, EXPECTED>(expectedValue,matchesBiPredicate,"",null,null);
  }
  
  public static <T,EXPECTED> VerboseCondition<T,EXPECTED> descriptive(EXPECTED expectedValue, BiPredicate<T, EXPECTED> matchesBiPredicate,
	      String matchDescription) {
	  return new VerboseCondition<T, EXPECTED>(expectedValue,matchesBiPredicate,matchDescription,null,null);
  }
  public static <T,EXPECTED> VerboseCondition<T,EXPECTED> verbose(EXPECTED expectedValue, BiPredicate<T, EXPECTED> matchesBiPredicate,
	      String matchDescription, Function<EXPECTED, ?> expectedValueTransformation,
	      Function<T, ?> givenValueTransformation) {
	  return new VerboseCondition<T, EXPECTED>(expectedValue,matchesBiPredicate,matchDescription,expectedValueTransformation,givenValueTransformation);
  }
  
  private VerboseCondition(EXPECTED expectedValue, BiPredicate<T, EXPECTED> matchesBiPredicate,
      String matchDescription, Function<EXPECTED, ?> expectedValueTransformation,
      Function<T, ?> givenValueTransformation) {
    this.expectedValue = expectedValue;
    this.matchesBiPredicate = matchesBiPredicate;
    this.matchDescription = matchDescription;
    this.expectedTransformation = expectedValueTransformation;
    this.givenValueTransformation = givenValueTransformation;
    describedAs("%s%s %s", defaultState().apply(State.NOT_EXECUTED), matchDescription,
        transformIf(expectedValueTransformation, expectedValue));
  }

  @Override
  public boolean matches(T value) {

    boolean match = matchesBiPredicate.test(value, expectedValue);
    if (match) {
      describedAs("%s%s <%s>", defaultState().apply(State.OK), matchDescription,
          transformIf(expectedTransformation, expectedValue));
    } else {
      describedAs("%s%s <%s> but was <%s>", defaultState().apply(State.FAILED), matchDescription,
          transformIf(expectedTransformation, expectedValue), transformIf(givenValueTransformation, value));
    }
    return match;
  }

  private static <E> Object transformIf(Function<E, ?> transform, E object) {

    return transform == null ? object : transform.apply(object);
  }
}
