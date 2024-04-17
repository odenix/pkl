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

import java.util.function.IntFunction;

public class FromGeneratorWithSizeArbitrary<T> implements Arbitrary<T> {

  private final IntFunction<RandomGenerator<T>> supplier;

  public FromGeneratorWithSizeArbitrary(IntFunction<RandomGenerator<T>> generatorSupplier) {
    this.supplier = generatorSupplier;
  }

  @Override
  public RandomGenerator<T> generator(final int genSize) {
    return supplier.apply(genSize);
  }

  @Override
  public EdgeCases<T> edgeCases(int maxEdgeCases) {
    return EdgeCases.none();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FromGeneratorWithSizeArbitrary<?> that = (FromGeneratorWithSizeArbitrary<?>) o;
    return LambdaSupport.areEqual(supplier, that.supplier);
  }

  @Override
  public int hashCode() {
    return supplier.getClass().hashCode();
  }
}
