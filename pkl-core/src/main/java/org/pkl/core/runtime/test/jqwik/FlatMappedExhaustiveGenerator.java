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

public class FlatMappedExhaustiveGenerator<U, T> implements ExhaustiveGenerator<U> {
  private final ExhaustiveGenerator<T> baseGenerator;
  private final long maxCount;
  private final Function<T, Arbitrary<U>> mapper;

  public static <T, U> Optional<Long> calculateMaxCounts(
      ExhaustiveGenerator<T> baseGenerator,
      Function<T, Arbitrary<U>> mapper,
      long maxNumberOfSamples) {
    long choices = 0;
    for (T baseValue : baseGenerator) {
      Optional<ExhaustiveGenerator<U>> exhaustive =
          mapper.apply(baseValue).exhaustive(maxNumberOfSamples);
      if (!exhaustive.isPresent()) {
        return Optional.empty();
      }
      choices += exhaustive.get().maxCount();
      if (choices > maxNumberOfSamples) {
        return Optional.empty();
      }
    }
    return Optional.of(choices);
  }

  public FlatMappedExhaustiveGenerator(
      ExhaustiveGenerator<T> baseGenerator, long maxCount, Function<T, Arbitrary<U>> mapper) {
    this.baseGenerator = baseGenerator;
    this.maxCount = maxCount;
    this.mapper = mapper;
  }

  @Override
  public long maxCount() {
    return maxCount;
  }

  @Override
  public Iterator<U> iterator() {
    List<Iterable<U>> iterators =
        StreamSupport.stream(baseGenerator.spliterator(), false)
            .map(baseValue -> (Iterable<U>) mapper.apply(baseValue).exhaustive().get())
            .collect(Collectors.toList());

    return Combinatorics.concat(iterators);
  }
}
