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

import static org.pkl.core.runtime.test.jqwik.MathSupport.factorial;

import java.util.*;

class PermutationExhaustiveGenerator<T> implements ExhaustiveGenerator<List<T>> {
  private final List<T> values;
  private final Long maxCount;

  public PermutationExhaustiveGenerator(List<T> values, Long maxCount) {
    this.values = values;
    this.maxCount = maxCount;
  }

  static <T> Optional<Long> calculateMaxCount(List<T> values, long maxNumberOfSamples) {
    try {
      long choices = factorial(values.size());
      if (choices > maxNumberOfSamples || choices < 0) {
        return Optional.empty();
      }
      return Optional.of(choices);
    } catch (ArithmeticException ae) {
      return Optional.empty();
    }
  }

  @Override
  public long maxCount() {
    return maxCount;
  }

  @Override
  public Iterator<List<T>> iterator() {
    return Combinatorics.listPermutations(values);
  }
}
