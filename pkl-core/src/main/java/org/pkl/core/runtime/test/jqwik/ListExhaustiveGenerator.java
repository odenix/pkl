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

class ListExhaustiveGenerator<T> implements ExhaustiveGenerator<List<T>> {
  private final Arbitrary<T> elementArbitrary;
  private final Long maxCount;
  private final int minSize;
  private final int maxSize;

  static Optional<Long> calculateMaxCount(
      Arbitrary<?> elementArbitrary, int minSize, int maxSize, long maxNumberOfSamples) {
    Optional<? extends ExhaustiveGenerator<?>> exhaustiveElement =
        elementArbitrary.exhaustive(maxNumberOfSamples);
    if (!exhaustiveElement.isPresent()) return Optional.empty();

    long elementMaxCount = exhaustiveElement.get().maxCount();
    long sum = 0;
    for (int n = minSize; n <= maxSize; n++) {
      double choices = Math.pow(elementMaxCount, n);
      if (choices > maxNumberOfSamples) { // Stop when break off point reached
        return Optional.empty();
      }
      sum += (long) choices;
    }
    return Optional.of(sum);
  }

  ListExhaustiveGenerator(Arbitrary<T> elementArbitrary, Long maxCount, int minSize, int maxSize) {
    this.elementArbitrary = elementArbitrary;
    this.maxCount = maxCount;
    this.minSize = minSize;
    this.maxSize = maxSize;
  }

  @Override
  public Iterator<List<T>> iterator() {
    return Combinatorics.listCombinations(elementArbitrary.exhaustive().get(), minSize, maxSize);
  }

  @Override
  public long maxCount() {
    return maxCount;
  }
}
