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

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultListCombinator<T> implements Combinators.ListCombinator<T> {

  protected final Arbitrary<T>[] arbitraries;

  @SuppressWarnings("unchecked")
  public DefaultListCombinator(List<Arbitrary<T>> listOfArbitraries) {
    this(listOfArbitraries.toArray(new Arbitrary[0]));
  }

  private DefaultListCombinator(Arbitrary<T>[] arbitraries) {
    this.arbitraries = arbitraries;
  }

  @Override
  public <R> Arbitrary<R> as(Function<List<T>, R> combinator) {
    return new CombineArbitrary<>(combineFunction(combinator), arbitraries);
  }

  public Combinators.ListCombinator<T> filter(Predicate<List<T>> filter) {
    return new Filtered<>(arbitraries, filter);
  }

  @SuppressWarnings("unchecked")
  protected <R> Function<List<Object>, R> combineFunction(Function<List<T>, R> combinator) {
    return params -> combinator.apply((List<T>) params);
  }

  private static class Filtered<T> extends DefaultListCombinator<T> {
    private final Predicate<List<T>> filter;

    private Filtered(Arbitrary<T>[] arbitraries, Predicate<List<T>> filter) {
      super(arbitraries);
      this.filter = filter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Arbitrary<R> as(Function<List<T>, R> combinator) {
      Predicate<List<Object>> filterPredicate = params -> filter.test((List<T>) params);
      return new CombineArbitrary<>(Function.identity(), arbitraries)
          .filter(filterPredicate)
          .map(combineFunction(combinator));
    }

    @Override
    public Combinators.ListCombinator<T> filter(Predicate<List<T>> filter) {
      return super.filter(combineFilters(this.filter, filter));
    }

    private Predicate<List<T>> combineFilters(Predicate<List<T>> first, Predicate<List<T>> second) {
      return first.and(second);
    }
  }
}
