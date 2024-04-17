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
import java.util.stream.Collectors;

public class FrequencyArbitrary<T> extends UseGeneratorsArbitrary<T> {
  private final List<Tuple.Tuple2<Integer, T>> frequencies;

  public FrequencyArbitrary(List<Tuple.Tuple2<Integer, T>> frequencies) {
    super(
        RandomGenerators.frequency(frequencies),
        max -> ExhaustiveGenerators.choose(valuesOf(frequencies), max),
        maxEdgeCases -> EdgeCasesSupport.choose(valuesOf(frequencies), maxEdgeCases));
    this.frequencies = frequencies;
  }

  private static <T> List<T> valuesOf(List<Tuple.Tuple2<Integer, T>> frequencies) {
    return frequencies.stream().map(Tuple.Tuple2::get2).collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FrequencyArbitrary<?> that = (FrequencyArbitrary<?>) o;
    return frequencies.equals(that.frequencies);
  }

  @Override
  public int hashCode() {
    return frequencies.hashCode();
  }
}
