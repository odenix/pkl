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
package org.pkl.core.runtime.test;

import org.pkl.core.util.Nullable;

final class RangeMatch<T> {
  private final @Nullable T exactValue;
  private final @Nullable T minValue;
  private final @Nullable T maxValue;

  RangeMatch(@Nullable T exactValue, @Nullable T minValue, @Nullable T maxValue) {
    this.exactValue = exactValue;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Nullable T exactValue() {
    return exactValue;
  }

  @Nullable T minValue() {
    return minValue;
  }

  @Nullable T maxValue() {
    return maxValue;
  }
}
