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

public class DefaultCombinator2<T1, T2> implements Combinators.Combinator2<T1, T2> {
  protected final Arbitrary<T2> a2;
  protected final Arbitrary<T1> a1;

  public DefaultCombinator2(Arbitrary<T1> a1, Arbitrary<T2> a2) {
    this.a1 = a1;
    this.a2 = a2;
  }

  @Override
  public <R> Arbitrary<R> as(Combinators.F2<T1, T2, R> combinator) {
    return new CombineArbitrary<>(combineFunction(combinator), a1, a2);
  }

  @Override
  public Combinators.Combinator2<T1, T2> filter(Combinators.F2<T1, T2, Boolean> filter) {
    return new Filtered<>(a1, a2, filter);
  }

  @SuppressWarnings("unchecked")
  protected <R> Function<List<Object>, R> combineFunction(Combinators.F2<T1, T2, R> combinator2) {
    return params -> combinator2.apply((T1) params.get(0), (T2) params.get(1));
  }

  private static class Filtered<T1, T2> extends DefaultCombinator2<T1, T2> {
    private final Combinators.F2<T1, T2, Boolean> filter;

    private Filtered(Arbitrary<T1> a1, Arbitrary<T2> a2, Combinators.F2<T1, T2, Boolean> filter) {
      super(a1, a2);
      this.filter = filter;
    }

    @Override
    public <R> Arbitrary<R> as(Combinators.F2<T1, T2, R> combinator) {
      return new CombineArbitrary<>(Function.identity(), a1, a2)
          .filter(combineFunction(filter)::apply)
          .map(combineFunction(combinator));
    }

    @Override
    public Combinators.Combinator2<T1, T2> filter(Combinators.F2<T1, T2, Boolean> filter) {
      return new Filtered<>(a1, a2, combineFilters(this.filter, filter));
    }

    private Combinators.F2<T1, T2, Boolean> combineFilters(
        Combinators.F2<T1, T2, Boolean> first, Combinators.F2<T1, T2, Boolean> second) {
      return (t1, t2) -> first.apply(t1, t2) && second.apply(t1, t2);
    }
  }
}
