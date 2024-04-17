/**
 * Copyright © 2024 Apple Inc. and the Pkl project authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pkl.core.runtime.test.jqwik;

import java.util.*;
import java.util.function.*;
import org.pkl.core.util.Nullable;

public class Combinators {

  public static class CombinatorsFacade {
    private static final CombinatorsFacade implementation;

    static {
      implementation = new CombinatorsFacade();
    }

    public <T1, T2> Combinators.Combinator2<T1, T2> combine2(Arbitrary<T1> a1, Arbitrary<T2> a2) {
      return new DefaultCombinator2<>(a1, a2);
    }

    public <T1, T2, T3> Combinators.Combinator3<T1, T2, T3> combine3(
        Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3) {
      return new DefaultCombinator3<>(a1, a2, a3);
    }

    public <T1, T2, T3, T4> Combinators.Combinator4<T1, T2, T3, T4> combine4(
        Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3, Arbitrary<T4> a4) {
      return new DefaultCombinator4<>(a1, a2, a3, a4);
    }

    public <T> Combinators.ListCombinator<T> combineList(List<Arbitrary<T>> listOfArbitraries) {
      return new DefaultListCombinator<>(listOfArbitraries);
    }
  }

  private Combinators() {}

  /**
   * Combine 2 arbitraries into one.
   *
   * @return Combinator2 instance which can be evaluated using {@linkplain Combinator2#as}
   */
  public static <@Nullable T1, @Nullable T2> Combinator2<T1, T2> combine(
      Arbitrary<T1> a1, Arbitrary<T2> a2) {
    return CombinatorsFacade.implementation.combine2(a1, a2);
  }

  /**
   * Combine 3 arbitraries into one.
   *
   * @return Combinator3 instance which can be evaluated using {@linkplain Combinator3#as}
   */
  public static <@Nullable T1, @Nullable T2, @Nullable T3> Combinator3<T1, T2, T3> combine(
      Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3) {
    return CombinatorsFacade.implementation.combine3(a1, a2, a3);
  }

  /**
   * Combine 4 arbitraries into one.
   *
   * @return Combinator4 instance which can be evaluated using {@linkplain Combinator4#as}
   */
  public static <@Nullable T1, @Nullable T2, @Nullable T3, @Nullable T4>
      Combinator4<T1, T2, T3, T4> combine(
          Arbitrary<T1> a1, Arbitrary<T2> a2, Arbitrary<T3> a3, Arbitrary<T4> a4) {
    return CombinatorsFacade.implementation.combine4(a1, a2, a3, a4);
  }

  /**
   * Combine a list of arbitraries into one.
   *
   * @return ListCombinator instance which can be evaluated using {@linkplain ListCombinator#as}
   */
  public static <@Nullable T> ListCombinator<T> combine(List<Arbitrary<T>> listOfArbitraries) {
    return CombinatorsFacade.implementation.combineList(listOfArbitraries);
  }

  /** Combinator for two values. */
  public interface Combinator2<@Nullable T1, @Nullable T2> {

    /**
     * Combine two values.
     *
     * @param combinator function
     * @param <R> return type
     * @return arbitrary instance
     */
    <@Nullable R> Arbitrary<R> as(F2<T1, T2, R> combinator);

    /**
     * Filter two values to only let them pass if the predicate is true.
     *
     * @param filter function
     * @return combinator instance
     */
    Combinator2<T1, T2> filter(F2<T1, T2, Boolean> filter);

    /**
     * Combine two values to create a new arbitrary.
     *
     * @param flatCombinator function
     * @param <R> return type of arbitrary
     * @return arbitrary instance
     */
    default <@Nullable R> Arbitrary<R> flatAs(F2<T1, T2, Arbitrary<R>> flatCombinator) {
      return as(flatCombinator).flatMap(Function.identity());
    }
  }

  /** Combinator for three values. */
  public interface Combinator3<@Nullable T1, @Nullable T2, @Nullable T3> {

    /**
     * Combine three values.
     *
     * @param combinator function
     * @param <R> return type
     * @return arbitrary instance
     */
    <@Nullable R> Arbitrary<R> as(F3<T1, T2, T3, R> combinator);

    /**
     * Filter three values to only let them pass if the predicate is true.
     *
     * @param filter function
     * @return combinator instance
     */
    Combinator3<T1, T2, T3> filter(F3<T1, T2, T3, Boolean> filter);

    /**
     * Combine three values to create a new arbitrary.
     *
     * @param flatCombinator function
     * @param <R> return type of arbitrary
     * @return arbitrary instance
     */
    default <@Nullable R> Arbitrary<R> flatAs(F3<T1, T2, T3, Arbitrary<R>> flatCombinator) {
      return as(flatCombinator).flatMap(Function.identity());
    }
  }

  /** Combinator for four values. */
  public interface Combinator4<@Nullable T1, @Nullable T2, @Nullable T3, @Nullable T4> {

    /**
     * Combine four values.
     *
     * @param combinator function
     * @param <R> return type
     * @return arbitrary instance
     */
    <@Nullable R> Arbitrary<R> as(F4<T1, T2, T3, T4, R> combinator);

    /**
     * Filter four values to only let them pass if the predicate is true.
     *
     * @param filter function
     * @return combinator instance
     */
    Combinator4<T1, T2, T3, T4> filter(F4<T1, T2, T3, T4, Boolean> filter);

    /**
     * Combine four values to create a new arbitrary.
     *
     * @param flatCombinator function
     * @param <R> return type of arbitrary
     * @return arbitrary instance
     */
    default <@Nullable R> Arbitrary<R> flatAs(F4<T1, T2, T3, T4, Arbitrary<R>> flatCombinator) {
      return as(flatCombinator).flatMap(Function.identity());
    }
  }

  /** Combinator for any number of values. */
  public interface ListCombinator<@Nullable T> {

    /**
     * Combine any number of values.
     *
     * @param combinator function
     * @param <R> return type
     * @return arbitrary instance
     */
    <@Nullable R> Arbitrary<R> as(Function<List<T>, R> combinator);

    /**
     * Filter list of values to only let them pass if the predicate is true.
     *
     * @param filter function
     * @return combinator instance
     */
    ListCombinator<T> filter(Predicate<List<T>> filter);

    /**
     * Combine list of values to create a new arbitrary.
     *
     * @param flatCombinator function
     * @param <R> return type of arbitrary
     * @return arbitrary instance
     */
    default <@Nullable R> Arbitrary<R> flatAs(Function<List<T>, Arbitrary<R>> flatCombinator) {
      return as(flatCombinator).flatMap(Function.identity());
    }
  }

  @FunctionalInterface
  public interface F2<@Nullable T1, @Nullable T2, @Nullable R> {
    R apply(T1 t1, T2 t2);
  }

  @FunctionalInterface
  public interface F3<@Nullable T1, @Nullable T2, @Nullable T3, @Nullable R> {
    R apply(T1 t1, T2 t2, T3 t3);
  }

  @FunctionalInterface
  public interface F4<@Nullable T1, @Nullable T2, @Nullable T3, @Nullable T4, @Nullable R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4);
  }
}
