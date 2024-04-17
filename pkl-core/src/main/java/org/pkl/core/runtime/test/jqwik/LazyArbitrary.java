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
import java.util.function.Supplier;

public class LazyArbitrary<T> implements Arbitrary<T> {
  private final Supplier<Arbitrary<T>> arbitrarySupplier;
  private Arbitrary<T> arbitrary;

  public LazyArbitrary(Supplier<Arbitrary<T>> arbitrarySupplier) {
    this.arbitrarySupplier = arbitrarySupplier;
  }

  @Override
  public RandomGenerator<T> generator(int genSize) {
    return getArbitrary().generator(genSize);
  }

  @Override
  public RandomGenerator<T> generatorWithEmbeddedEdgeCases(int genSize) {
    // This is actually right. Don't use getArbitrary().generatorWithEmbeddedEdgeCases() directly
    return getArbitrary().generator(genSize, true);
  }

  private Arbitrary<T> getArbitrary() {
    if (this.arbitrary == null) {
      this.arbitrary = arbitrarySupplier.get();
    }
    return this.arbitrary;
  }

  @Override
  public Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
    return getArbitrary().exhaustive(maxNumberOfSamples);
  }

  @Override
  public EdgeCases<T> edgeCases(int maxEdgeCases) {
    // Cannot be delegated to getArbitrary() due to possible recursion
    return EdgeCases.none();
  }
}
