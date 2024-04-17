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
import java.util.function.Predicate;

public class ArbitraryCollect<T> implements Arbitrary<List<T>> {

  private final Arbitrary<T> elementArbitrary;
  private final Predicate<List<T>> until;

  public ArbitraryCollect(Arbitrary<T> elementArbitrary, Predicate<List<T>> until) {
    this.elementArbitrary = elementArbitrary;
    this.until = until;
  }

  @Override
  public RandomGenerator<List<T>> generator(final int genSize) {
    return elementArbitrary.generator(genSize).collect(until);
  }

  @Override
  public EdgeCases<List<T>> edgeCases(int maxEdgeCases) {
    return EdgeCases.none();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArbitraryCollect<?> that = (ArbitraryCollect<?>) o;
    if (!elementArbitrary.equals(that.elementArbitrary)) return false;
    return LambdaSupport.areEqual(until, that.until);
  }

  @Override
  public int hashCode() {
    return HashCodeSupport.hash(elementArbitrary, until.getClass());
  }
}
