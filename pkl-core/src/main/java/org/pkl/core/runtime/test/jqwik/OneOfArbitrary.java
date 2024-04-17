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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.pkl.core.runtime.test.jqwik.Tuple.Tuple2;

public class OneOfArbitrary<T> implements Arbitrary<T> {
  private final List<Arbitrary<T>> all = new ArrayList<>();
  private final boolean isGeneratorMemoizable;

  @SuppressWarnings("unchecked")
  public OneOfArbitrary(Collection<Arbitrary<? extends T>> choices) {
    for (Arbitrary<? extends T> choice : choices) {
      all.add((Arbitrary<T>) choice);
    }
    isGeneratorMemoizable = all.stream().allMatch(Arbitrary::isGeneratorMemoizable);
  }

  @Override
  public RandomGenerator<T> generator(int genSize) {
    return rawGeneration(genSize, false);
  }

  @Override
  public RandomGenerator<T> generatorWithEmbeddedEdgeCases(int genSize) {
    return rawGeneration(genSize, true);
  }

  @Override
  public boolean isGeneratorMemoizable() {
    return isGeneratorMemoizable;
  }

  private RandomGenerator<T> rawGeneration(int genSize, boolean withEmbeddedEdgeCases) {
    List<Tuple2<Integer, Arbitrary<T>>> frequencies =
        all.stream().map(a -> Tuple.of(1, a)).collect(Collectors.toList());
    return RandomGenerators.frequencyOf(frequencies, genSize, withEmbeddedEdgeCases);
  }

  @Override
  public Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
    return ExhaustiveGenerators.choose(all, maxNumberOfSamples)
        .flatMap(
            generator ->
                ExhaustiveGenerators.flatMap(generator, Function.identity(), maxNumberOfSamples));
  }

  @Override
  public EdgeCases<T> edgeCases(int maxEdgeCases) {
    return EdgeCasesSupport.concatFrom(all, maxEdgeCases);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OneOfArbitrary<?> that = (OneOfArbitrary<?>) o;
    return all.equals(that.all);
  }

  @Override
  public int hashCode() {
    return all.hashCode();
  }
}
