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
 * Dynamic Condition that shows the expected and the tested value in the
 * description.
 */
public final class DynamicCondtion<T, EXPECTED> extends Condition<T> {

  private EXPECTED expected;

  private BiPredicate<T, EXPECTED> check;

  private String checkDescription;

  private Function<T, ?> transformGiven;

  private Function<EXPECTED, ?> transformExpected;

  private Function<State, String> transformState;

  public enum State {
    NOT_EXECUTED, OK, FAILED
  }

  public static Function<State, String> defaultState() {

    return state -> {
      switch (state) {
        case NOT_EXECUTED:
          return "[NOT EXECUTED]";
        case OK:
          return "[OK] ";
        case FAILED:
          return "[FAILED] ";
        default:
          return "[UNKNOWN] ";
      }
    };

  }

  protected DynamicCondtion(EXPECTED expected, BiPredicate<T, EXPECTED> check,
      String checkDescription, Function<EXPECTED, ?> transformExpected,
      Function<T, ?> transformGiven, Function<State, String> transformState) {

    this.expected = expected;
    this.check = check;
    this.checkDescription = checkDescription;
    this.transformExpected = transformExpected;
    this.transformGiven = transformGiven;
    this.transformState = transformState == null ? defaultState() : transformState;
    describedAs("%s%s %s", this.transformState.apply(State.NOT_EXECUTED), checkDescription,
        transformIf(transformExpected, expected));
  }

  @Override
  public boolean matches(T value) {

    boolean match = check.test(value, expected);
    if (match) {
      describedAs("%s%s <%s>", transformState.apply(State.OK), checkDescription,
          transformIf(transformExpected, expected));
    } else {
      describedAs("%s%s <%s> but was <%s>", transformState.apply(State.FAILED), checkDescription,
          transformIf(transformExpected, expected), transformIf(transformGiven, value));
    }
    return match;
  }

  private static <E> Object transformIf(Function<E, ?> transform, E object) {

    return transform == null ? object : transform.apply(object);
  }

  public static <T, EXPECTED> BuilderOptional<T, EXPECTED> ofIsNullCheck() {

    return new BuilderImpl<T, EXPECTED>().withCheckDescription("is null").withExpectedValue(null);
  }

  public static <T, EXPECTED> BuilderOptional<T, EXPECTED> ofIsNotNullCheck() {

    return new BuilderImpl<T, EXPECTED>().withCheckDescription("is not null")
        .withExpectedValue(null);
  }

  public static <T, EXPECTED> BuilderNeedsExpected<T, EXPECTED> ofCheckExpected(
      BiPredicate<T, EXPECTED> check, EXPECTED expected) {

    return new BuilderImpl<T, EXPECTED>().withCheckDescription("matches").checkThat(check);
  }

  public static <T, EXPECTED> BuilderNeedsExpected<T, EXPECTED> ofCheck(
      BiPredicate<T, EXPECTED> check) {

    return new BuilderImpl<T, EXPECTED>().withCheckDescription("matches").checkThat(check);
  }

  public static <T, EXPECTED> BuilderOptional<T, EXPECTED> ofIsEqualToCheck(EXPECTED expected) {

    return new BuilderImpl<T, EXPECTED>().withCheckDescription("is equal to")
        .withExpectedValue(expected);
  }

  public static <T, EXPECTED> BuilderNeedsExpected<T, EXPECTED> ofIsEqualCheck() {

    return new BuilderImpl<T, EXPECTED>().withCheckDescription("is equal to");
  }

  interface BuilderNeedsExpected<T, EXPECTED> {

    BuilderOptional<T, EXPECTED> withExpectedValue(EXPECTED expected);
  }

  interface BuilderNeedsCheck<T, EXPECTED> {

    BuilderNeedsExpected<T, EXPECTED> checkThat(BiPredicate<T, EXPECTED> check);
  }

  interface BuilderOptional<T, EXPECTED> {

    BuilderOptional<T, EXPECTED> withExpectedValueDescription(
        Function<EXPECTED, ?> checkDescription);

    BuilderImpl<T, EXPECTED> withStateDescription(Function<State, String> transform);

    BuilderOptional<T, EXPECTED> withCheckedValueDescription(Function<T, ?> transformButWas);

    BuilderOptional<T, EXPECTED> withCheckDescription(String predicateText);

    Condition<T> build();
  }

  static class BuilderImpl<T, EXPECTED> implements BuilderOptional<T, EXPECTED>,
      BuilderNeedsExpected<T, EXPECTED>, BuilderNeedsCheck<T, EXPECTED> {

    private EXPECTED expected;

    private BiPredicate<T, EXPECTED> check;

    private String checkDescription;

    private Function<T, ?> transformGiven;

    private Function<EXPECTED, ?> transformExpected;

    Function<State, String> transformState;

    public BuilderImpl() {

    }

    @Override
    public Condition<T> build() {

      return new DynamicCondtion<T, EXPECTED>(expected, check, checkDescription, transformExpected,
          transformGiven, transformState);
    }

    @Override
    public BuilderImpl<T, EXPECTED> checkThat(BiPredicate<T, EXPECTED> check) {

      this.check = check;
      return this;
    }

    @Override
    public BuilderImpl<T, EXPECTED> withExpectedValueDescription(
        Function<EXPECTED, ?> transformExpected) {

      this.transformExpected = transformExpected;
      return this;
    }

    @Override
    public BuilderImpl<T, EXPECTED> withStateDescription(Function<State, String> transform) {

      this.transformState = transform;
      return this;
    }

    @Override
    public BuilderImpl<T, EXPECTED> withCheckedValueDescription(Function<T, ?> transform) {

      this.transformGiven = transform;
      return this;
    }

    @Override
    public BuilderImpl<T, EXPECTED> withCheckDescription(String checkDescription) {

      this.checkDescription = checkDescription;
      return this;
    }

    @Override
    public BuilderImpl<T, EXPECTED> withExpectedValue(EXPECTED expected) {

      this.expected = expected;
      return this;
    }
  }
}
