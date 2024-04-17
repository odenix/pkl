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

class SetExhaustiveGenerator<T> implements ExhaustiveGenerator<Set<T>> {
  private final Arbitrary<T> elementArbitrary;
  private final long maxCount;
  private final int minSize;
  private final int maxSize;

  static Optional<Long> calculateMaxCount(
      Arbitrary<?> elementArbitrary, int minSize, int maxSize, long maxNumberOfSamples) {
    Optional<? extends ExhaustiveGenerator<?>> exhaustiveElement =
        elementArbitrary.exhaustive(maxNumberOfSamples);
    if (!exhaustiveElement.isPresent()) return Optional.empty();

    long elementMaxCount = exhaustiveElement.get().maxCount();
    long sum = 0;
    for (int k = minSize; k <= maxSize; k++) {
      if (k == 0) { // empty set
        sum += 1;
        continue;
      }
      if (elementMaxCount < k) { // empty set
        continue;
      }
      if (elementMaxCount > 70) {
        // MathSupport.binomial() only works till 70
        return Optional.empty();
      }
      long choices = 0;
      try {
        choices = MathSupport.binomial(Math.toIntExact(elementMaxCount), k);
      } catch (ArithmeticException ae) {
        return Optional.empty();
      }
      if (choices < 0) {
        return Optional.empty();
      }
      sum += choices;
      if (sum > maxNumberOfSamples) { // Stop when break off point reached
        return Optional.empty();
      }
    }
    return Optional.of(sum);
  }

  SetExhaustiveGenerator(Arbitrary<T> elementArbitrary, long maxCount, int minSize, int maxSize) {
    this.elementArbitrary = elementArbitrary;
    this.minSize = minSize;
    this.maxSize = maxSize;
    this.maxCount = maxCount;
  }

  @Override
  public Iterator<Set<T>> iterator() {
    return Combinatorics.setCombinations(elementArbitrary.exhaustive().get(), minSize, maxSize);
  }

  @Override
  public long maxCount() {
    return maxCount;
  }
}
