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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class JustArbitrary<T> implements Arbitrary<T> {

  private final T value;

  public JustArbitrary(T value) {
    this.value = value;
  }

  @Override
  public <U> Arbitrary<U> flatMap(Function<T, Arbitrary<U>> mapper) {
    // Optimization: just(value).flatMap(mapper) -> mapper(value)
    return mapper.apply(value);
  }

  @Override
  public <U> Arbitrary<U> map(Function<T, U> mapper) {
    // Optimization: just(value).map(mapper) -> just(mapper(value))
    return new JustArbitrary<>(mapper.apply(value));
  }

  @Override
  public RandomGenerator<T> generator(int tries) {
    return random -> Shrinkable.unshrinkable(value);
  }

  @Override
  public Optional<ExhaustiveGenerator<T>> exhaustive(long maxNumberOfSamples) {
    return ExhaustiveGenerators.choose(Arrays.asList(value), maxNumberOfSamples);
  }

  @Override
  public EdgeCases<T> edgeCases(int maxEdgeCases1) {
    return maxEdgeCases1 <= 0
        ? EdgeCases.none()
        : EdgeCases.fromSupplier(() -> Shrinkable.unshrinkable(value));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JustArbitrary<?> that = (JustArbitrary<?>) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }
}
