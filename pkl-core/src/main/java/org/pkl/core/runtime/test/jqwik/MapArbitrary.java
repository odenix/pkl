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
 * Map}
 */
public interface MapArbitrary<K, V> extends Arbitrary<Map<K, V>>, SizableArbitrary<Map<K, V>> {

  /**
   * Fix the size to {@code size}.
   *
   * @param size The size of the generated map
   * @return new arbitrary instance
   */
  @Override
  default MapArbitrary<K, V> ofSize(int size) {
    return ofMinSize(size).ofMaxSize(size);
  }

  /**
   * Set lower size boundary {@code minSize} (included).
   *
   * @param minSize The minimum size of the generated map
   * @return new arbitrary instance
   */
  MapArbitrary<K, V> ofMinSize(int minSize);

  /**
   * Set upper size boundary {@code maxSize} (included).
   *
   * @param maxSize The maximum size of the generated map
   * @return new arbitrary instance
   */
  MapArbitrary<K, V> ofMaxSize(int maxSize);

  /**
   * Set random distribution {@code distribution} of size of generated map. The distribution's
   * center is the minimum size of the generated map.
   */
  MapArbitrary<K, V> withSizeDistribution(RandomDistribution uniform);

  /**
   * Add the constraint that keys of the generated map must be unique relating to an element's
   * "feature" being extracted by applying the {@code by} function on a map entry's key. The
   * extracted features are being compared using {@linkplain Object#equals(Object)}.
   *
   * <p>The constraint can be combined with other {@linkplain #uniqueKeys(Function)} constraints.
   *
   * @return new arbitrary instance
   */
  MapArbitrary<K, V> uniqueKeys(Function<K, Object> by);

  /**
   * Add the constraint that value of the generated map must be unique relating to an element's
   * "feature" being extracted by applying the {@code by} function on a map entry's value. The
   * extracted features are being compared using {@linkplain Object#equals(Object)}.
   *
   * <p>The constraint can be combined with other {@linkplain #uniqueValues(Function)} constraints.
   *
   * @return new arbitrary instance
   */
  MapArbitrary<K, V> uniqueValues(Function<V, Object> by);

  /**
   * Add the constraint that values of the generated map must be unique, i.e. no two value must
   * return true when being compared using {@linkplain Object#equals(Object)}.
   *
   * <p>The constraint can be combined with other {@linkplain #uniqueValues(Function)} constraints.
   *
   * @return new arbitrary instance
   */
  MapArbitrary<K, V> uniqueValues();
}
