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
import java.util.stream.*;

class CombinedExhaustiveGenerator<R> implements ExhaustiveGenerator<R> {
  private final Long maxCount;
  private final List<Arbitrary<Object>> arbitraries;
  private final Function<List<Object>, R> combinator;

  static Optional<Long> calculateMaxCount(
      List<Arbitrary<Object>> arbitraries, long maxNumberOfSamples) {
    long product = 1;
    for (Arbitrary<Object> arbitrary : arbitraries) {
      Optional<ExhaustiveGenerator<Object>> exhaustive = arbitrary.exhaustive(maxNumberOfSamples);
      if (!exhaustive.isPresent()) {
        return Optional.empty();
      }
      product *= exhaustive.get().maxCount();
      if (product > maxNumberOfSamples) {
        return Optional.empty();
      }
    }
    return Optional.of(product);
  }

  CombinedExhaustiveGenerator(
      Long maxCount, List<Arbitrary<Object>> arbitraries, Function<List<Object>, R> combinator) {
    this.maxCount = maxCount;
    this.arbitraries = arbitraries;
    this.combinator = combinator;
  }

  @Override
  public long maxCount() {
    return maxCount;
  }

  @Override
  public Iterator<R> iterator() {
    List<Iterable<Object>> iterables =
        arbitraries.stream()
            .map(a -> (Iterable<Object>) a.exhaustive().get())
            .collect(Collectors.toList());
    Iterator<List<Object>> valuesIterator = Combinatorics.combine(iterables);

    return new Iterator<R>() {
      @Override
      public boolean hasNext() {
        return valuesIterator.hasNext();
      }

      @Override
      public R next() {
        List<Object> values = valuesIterator.next();
        return combinator.apply(values);
      }
    };
  }
}
