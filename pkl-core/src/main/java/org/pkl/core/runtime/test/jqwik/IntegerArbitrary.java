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

/** Fluent interface to configure the generation of Integer and int values. */
public interface IntegerArbitrary extends NumericalArbitrary<Integer, IntegerArbitrary> {

  /**
   * Set the allowed lower {@code min} (included) and upper {@code max} (included) bounder of
   * generated numbers.
   */
  default IntegerArbitrary between(int min, int max) {
    return greaterOrEqual(min).lessOrEqual(max);
  }

  /** Set the allowed lower {@code min} (included) bounder of generated numbers. */
  IntegerArbitrary greaterOrEqual(int min);

  /** Set the allowed upper {@code max} (included) bounder of generated numbers. */
  IntegerArbitrary lessOrEqual(int max);

  /** Set shrinking target to {@code target} which must be between the allowed bounds. */
  IntegerArbitrary shrinkTowards(int target);
}
