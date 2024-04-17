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
import java.util.function.Predicate;

public class ArbitraryFilter<T> extends ArbitraryDelegator<T> {
  private final Predicate<T> filterPredicate;
  private final int maxMisses;

  public ArbitraryFilter(Arbitrary<T> self, Predicate<T> filterPredicate, int maxMisses) {
    super(self);
    this.filterPredicate = filterPredicate;
    this.maxMisses = maxMisses;
  }

  @Override
  public RandomGenerator<T> generator(int genSize) {
    return super.generator(genSize).filter(filterPredicate, maxMisses);
  }

  @Override
  public RandomGenerator<T> generatorWithEmbeddedEdgeCases(int genSize) {
    return super.generatorWithEmbeddedEdgeCases(genSize).filter(filterPredicate, maxMisses);
  }

  @Override
  public Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
    return super.exhaustive(maxNumberOfSamples)
        .map(generator -> generator.filter(filterPredicate, maxMisses));
  }

  @Override
  public EdgeCases<T> edgeCases(int maxEdgeCases) {
    return EdgeCasesSupport.filter(super.edgeCases(maxEdgeCases), filterPredicate);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!super.equals(o)) return false;

    ArbitraryFilter<?> that = (ArbitraryFilter<?>) o;
    if (maxMisses != that.maxMisses) return false;
    return LambdaSupport.areEqual(filterPredicate, that.filterPredicate);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + maxMisses;
    return result;
  }
}
