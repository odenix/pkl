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
import java.util.stream.*;
import org.pkl.core.util.Nullable;

/**
 * Fluent interface to add functionality to arbitraries whose generation artefacts can be streamed,
 * e.g. {@link List}, {@link Set}, {@link Stream} and Arrays
 */
public interface StreamableArbitrary<T, U> extends SizableArbitrary<U> {

  /**
   * Given an {@code initial} argument use {@code accumulator} to produce the final result.
   *
   * @param initial The initial argument. Also the result if streamable is empty
   * @param accumulator The function used to reduce a streamable into a result one by one
   * @param <R> The result type
   * @return The result of accumulating all elements in streamable
   */
  <R> Arbitrary<R> reduce(R initial, BiFunction<R, T, R> accumulator);

  /** Fix the size to {@code size}. */
  default StreamableArbitrary<T, U> ofSize(int size) {
    return ofMinSize(size).ofMaxSize(size);
  }

  /** Set lower size boundary {@code minSize} (included). */
  StreamableArbitrary<T, U> ofMinSize(int minSize);

  /** Set upper size boundary {@code maxSize} (included). */
  StreamableArbitrary<T, U> ofMaxSize(int maxSize);

  /** Set distribution {@code distribution} of size of generated arbitrary */
  StreamableArbitrary<T, U> withSizeDistribution(RandomDistribution distribution);

  /**
   * Add the constraint that elements of the generated streamable must be unique, i.e. no two
   * elements must return true when being compared using {@linkplain Object#equals(Object)}.
   *
   * <p>The constraint can be combined with other {@linkplain #uniqueElements(Function)}
   * constraints.
   *
   * @return new arbitrary instance
   */
  StreamableArbitrary<T, U> uniqueElements();

  /**
   * Add the constraint that elements of the generated streamable must be unique relating to an
   * element's "feature" being extracted using the {@code by} function. The extracted features are
   * being compared using {@linkplain Object#equals(Object)}.
   *
   * <p>The constraint can be combined with other {@linkplain #uniqueElements(Function)}
   * constraints.
   *
   * @return new arbitrary instance
   */
  StreamableArbitrary<@Nullable T, U> uniqueElements(Function<@Nullable T, Object> by);
}
