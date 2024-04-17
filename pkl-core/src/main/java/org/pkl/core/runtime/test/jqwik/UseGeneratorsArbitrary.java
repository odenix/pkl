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

abstract class UseGeneratorsArbitrary<T> implements Arbitrary<T> {

  private final RandomGenerator<T> randomGenerator;
  private final Function<Long, Optional<ExhaustiveGenerator<T>>> exhaustiveGeneratorFunction;
  private final Function<Integer, EdgeCases<T>> edgeCasesSupplier;

  public UseGeneratorsArbitrary(
      RandomGenerator<T> randomGenerator,
      Function<Long, Optional<ExhaustiveGenerator<T>>> exhaustiveGeneratorFunction,
      Function<Integer, EdgeCases<T>> edgeCasesSupplier) {
    this.randomGenerator = randomGenerator;
    this.exhaustiveGeneratorFunction = exhaustiveGeneratorFunction;
    this.edgeCasesSupplier = edgeCasesSupplier;
  }

  @Override
  public RandomGenerator<T> generator(int tries) {
    return randomGenerator;
  }

  @Override
  public Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
    return exhaustiveGeneratorFunction.apply(maxNumberOfSamples);
  }

  @Override
  public EdgeCases<T> edgeCases(int maxEdgeCases) {
    return maxEdgeCases <= 0 ? EdgeCases.none() : edgeCasesSupplier.apply(maxEdgeCases);
  }
}
