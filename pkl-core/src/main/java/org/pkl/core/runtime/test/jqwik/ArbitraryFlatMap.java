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

import java.util.Optional;
import java.util.function.Function;

public class ArbitraryFlatMap<T, U> implements Arbitrary<U> {
  private final Arbitrary<T> self;
  private final Function<T, Arbitrary<U>> mapper;

  public ArbitraryFlatMap(Arbitrary<T> self, Function<T, Arbitrary<U>> mapper) {
    this.self = self;
    this.mapper = mapper;
  }

  @Override
  public RandomGenerator<U> generator(int genSize) {
    return self.generator(genSize).flatMap(mapper, genSize, false);
  }

  @Override
  public RandomGenerator<U> generatorWithEmbeddedEdgeCases(int genSize) {
    return self.generatorWithEmbeddedEdgeCases(genSize).flatMap(mapper, genSize, true);
  }

  @Override
  public Optional<ExhaustiveGenerator<U>> exhaustive(long maxNumberOfSamples) {
    return self.exhaustive(maxNumberOfSamples)
        .flatMap(generator -> ExhaustiveGenerators.flatMap(generator, mapper, maxNumberOfSamples));
  }

  @Override
  public EdgeCases<U> edgeCases(int maxEdgeCases) {
    return EdgeCasesSupport.flatMapArbitrary(self.edgeCases(maxEdgeCases), mapper, maxEdgeCases);
  }

  @Override
  public boolean isGeneratorMemoizable() {
    return self.isGeneratorMemoizable();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArbitraryFlatMap<?, ?> that = (ArbitraryFlatMap<?, ?>) o;
    if (!self.equals(that.self)) return false;
    return LambdaSupport.areEqual(mapper, that.mapper);
  }

  @Override
  public int hashCode() {
    return HashCodeSupport.hash(self, mapper.getClass());
  }
}
