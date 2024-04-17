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

import java.util.*;
import java.util.function.*;

/**
 * Fluent interface to add functionality to arbitraries that generate instances of type {@linkplain
 * Set}
 */
public interface SetArbitrary<T> extends Arbitrary<Set<T>>, StreamableArbitrary<T, Set<T>> {

  /**
   * Fix the size to {@code size}.
   *
   * @param size The size of the generated set
   * @return new arbitrary instance
   */
  @Override
  default SetArbitrary<T> ofSize(int size) {
    return this.ofMinSize(size).ofMaxSize(size);
  }

  /**
   * Set lower size boundary {@code minSize} (included).
   *
   * @param minSize The minimum size of the generated set
   * @return new arbitrary instance
   */
  SetArbitrary<T> ofMinSize(int minSize);

  /**
   * Set upper size boundary {@code maxSize} (included).
   *
   * @param maxSize The maximum size of the generated set
   * @return new arbitrary instance
   */
  SetArbitrary<T> ofMaxSize(int maxSize);

  /**
   * Set random distribution {@code distribution} of size of generated set. The distribution's
   * center is the minimum size of the generated set.
   */
  SetArbitrary<T> withSizeDistribution(RandomDistribution uniform);

  /**
   * Map over each element of the generated set giving access to all elements of the set. The number
   * of the mapped elements within the set will stay the same.
   *
   * @param <U> The target type of a set to generate
   * @param mapper Mapper function to element type U
   * @return arbitrary of a set of Us
   */
  <U> Arbitrary<Set<U>> mapEach(BiFunction<Set<T>, T, U> mapper);

  /**
   * Flat-map over each element of the generated set giving access to all elements of the set. The
   * number of the mapped elements within the set will stay the same.
   *
   * @param <U> The target type of a set to generate
   * @param flatMapper Mapper function to arbitrary of element type U
   * @return arbitrary of a set of Us
   */
  <U> Arbitrary<Set<U>> flatMapEach(BiFunction<Set<T>, T, Arbitrary<U>> flatMapper);

  /**
   * Do not use. Sets have unique elements anyway. It only exists for purposes of symmetry.
   *
   * @return same instance of arbitrary
   */
  SetArbitrary<T> uniqueElements();

  /**
   * Add the constraint that elements of the generated set must be unique relating to an element's
   * "feature" being extracted using the {@code by} function. The extracted features are being
   * compared using {@linkplain Object#equals(Object)}.
   *
   * <p>The constraint can be combined with other {@linkplain #uniqueElements(Function)}
   * constraints.
   *
   * @return new arbitrary instance
   */
  SetArbitrary<T> uniqueElements(Function<T, Object> by);
}
