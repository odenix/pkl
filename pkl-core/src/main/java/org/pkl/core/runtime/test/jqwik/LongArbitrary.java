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

/** Fluent interface to configure the generation of Long and long values. */
public interface LongArbitrary extends NumericalArbitrary<Long, LongArbitrary> {

  /**
   * Set the allowed lower {@code min} (included) and upper {@code max} (included) bounds of
   * generated numbers.
   */
  default LongArbitrary between(long min, long max) {
    return greaterOrEqual(min).lessOrEqual(max);
  }

  /** Set the allowed lower {@code min} (included) bound of generated numbers. */
  LongArbitrary greaterOrEqual(long min);

  /** Set the allowed upper {@code max} (included) bound of generated numbers. */
  LongArbitrary lessOrEqual(long max);

  /** Set shrinking target to {@code target} which must be between the allowed bounds. */
  LongArbitrary shrinkTowards(long target);
}
