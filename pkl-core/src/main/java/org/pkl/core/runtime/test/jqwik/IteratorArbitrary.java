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
 * Iterator}
 */
public interface IteratorArbitrary<T>
    extends Arbitrary<Iterator<T>>, StreamableArbitrary<T, Iterator<T>> {

  /**
   * Fix the size to {@code size}.
   *
   * @param size The size of the generated iterator
   * @return new arbitrary instance
   */
  @Override
  default IteratorArbitrary<T> ofSize(int size) {
    return ofMinSize(size).ofMaxSize(size);
  }

  /**
   * Set lower size boundary {@code minSize} (included).
   *
   * @param minSize The minimum size of the generated iterator
   * @return new arbitrary instance
   */
  IteratorArbitrary<T> ofMinSize(int minSize);

  /**
   * Set upper size boundary {@code maxSize} (included).
   *
   * @param maxSize The maximum size of the generated iterator
   * @return new arbitrary instance
   */
  IteratorArbitrary<T> ofMaxSize(int maxSize);

  /**
   * Set random distribution {@code distribution} of size of generated iterator. The distribution's
   * center is the minimum size of the generated iterator.
   */
  IteratorArbitrary<T> withSizeDistribution(RandomDistribution uniform);

  /**
   * Add the constraint that elements of the generated iterator must be unique, i.e. no two elements
   * must return true when being compared using {@linkplain Object#equals(Object)}.
   *
   * <p>The constraint can be combined with other {@linkplain #uniqueElements(Function)}
   * constraints.
   *
   * @return new arbitrary instance
   */
  IteratorArbitrary<T> uniqueElements();

  /**
   * Add the constraint that elements of the generated iterator must be unique relating to an
   * element's "feature" being extracted using the {@code by} function. The extracted features are
   * being compared using {@linkplain Object#equals(Object)}.
   *
   * <p>The constraint can be combined with other {@linkplain #uniqueElements(Function)}
   * constraints.
   *
   * @return new arbitrary instance
   */
  IteratorArbitrary<T> uniqueElements(Function<T, Object> by);
}
