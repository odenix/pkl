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

/**
 * Fluent interface to configure arbitraries that have size constraints for generated values, e.g.
 * collections and arrays.
 */
public interface SizableArbitrary<U> extends Arbitrary<U> {

  /** Fix the size to {@code size}. */
  default SizableArbitrary<U> ofSize(int size) {
    return ofMinSize(size).ofMaxSize(size);
  }

  /** Set lower size boundary {@code minSize} (included). */
  SizableArbitrary<U> ofMinSize(int minSize);

  /** Set upper size boundary {@code maxSize} (included). */
  SizableArbitrary<U> ofMaxSize(int maxSize);

  /** Set distribution {@code distribution} of size of generated arbitrary */
  SizableArbitrary<U> withSizeDistribution(RandomDistribution distribution);
}
