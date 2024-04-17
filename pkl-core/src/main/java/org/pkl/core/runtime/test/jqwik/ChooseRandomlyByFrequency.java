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

public class ChooseRandomlyByFrequency<T> implements Function<Random, T> {

  private int[] upperBounds;
  private int size = 0;
  private List<T> valuesToChooseFrom;

  public ChooseRandomlyByFrequency(List<Tuple.Tuple2<Integer, T>> frequencies) {
    calculateUpperBorders(frequencies);
    if (size <= 0) {
      throw new JqwikException(
          String.format(
              "%s does not contain any positive frequencies.",
              JqwikStringSupport.displayString(frequencies)));
    }
  }

  protected List<T> possibleValues() {
    return valuesToChooseFrom;
  }

  private void calculateUpperBorders(List<Tuple.Tuple2<Integer, T>> frequencies) {
    List<T> values = new ArrayList<>(frequencies.size());
    // Zero-frequency elements are possible, so we start with a list, and transform to array later
    List<Integer> upperBounds = new ArrayList<>(frequencies.size());
    for (Tuple.Tuple2<Integer, T> tuple : frequencies) {
      int frequency = tuple.get1();
      if (frequency <= 0) continue;
      size = Math.addExact(size, frequency);
      T value = tuple.get2();
      values.add(value);
      upperBounds.add(size);
    }
    valuesToChooseFrom = values;
    this.upperBounds = upperBounds.stream().mapToInt(i -> i).toArray();
  }

  private T choose(int index) {
    int i = Arrays.binarySearch(upperBounds, index);
    if (i < 0) {
      // Exact value not found => convert "negative" insertion point to the actual index
      i = -(i + 1);
    } else {
      // Exact value is found, use the next bucket
      // For instance, if weights are {2,3}, the upperBounds are {2,5},
      // and the input indices could be 0..4.
      // "index" {0, 1} should be mapped to the first element
      // "index" {2, 3, 4} should be mapped to the second
      // If (input) index==2, binarySearch returns 0, so we must advance i by one
      // This advance is never triggered for the very last element since random.nextInt
      // excludes upper bound.
      i++;
    }
    return valuesToChooseFrom.get(i);
  }

  public T apply(Random random) {
    return choose(random.nextInt(size));
  }
}
