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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.pkl.core.runtime.test.jqwik.Tuple.Tuple2;

public class FrequencyOfArbitrary<T> implements Arbitrary<T> {

  private final List<Tuple2<Integer, Arbitrary<T>>> frequencies;
  private final boolean isGeneratorMemoizable;

  public FrequencyOfArbitrary(List<Tuple2<Integer, Arbitrary<T>>> frequencies) {
    this.frequencies = frequencies;
    this.isGeneratorMemoizable =
        frequencies.stream().allMatch(t -> t.get2().isGeneratorMemoizable());
    if (this.frequencies.isEmpty()) {
      throw new JqwikException("At least one frequency must be above 0");
    }
  }

  @Override
  public RandomGenerator<T> generator(int genSize) {
    return RandomGenerators.frequencyOf(frequencies, genSize, false);
  }

  @Override
  public RandomGenerator<T> generatorWithEmbeddedEdgeCases(int genSize) {
    return RandomGenerators.frequencyOf(frequencies, genSize, true);
  }

  @Override
  public boolean isGeneratorMemoizable() {
    return isGeneratorMemoizable;
  }

  @Override
  public Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
    return ExhaustiveGenerators.choose(allArbitraries(), maxNumberOfSamples)
        .flatMap(
            generator ->
                ExhaustiveGenerators.flatMap(generator, Function.identity(), maxNumberOfSamples));
  }

  @Override
  public EdgeCases<T> edgeCases(int maxEdgeCases) {
    return EdgeCasesSupport.concatFrom(allArbitraries(), maxEdgeCases);
  }

  private List<Arbitrary<T>> allArbitraries() {
    return frequencies.stream().map(Tuple2::get2).collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FrequencyOfArbitrary<?> that = (FrequencyOfArbitrary<?>) o;
    return frequencies.equals(that.frequencies);
  }

  @Override
  public int hashCode() {
    return frequencies.hashCode();
  }
}
