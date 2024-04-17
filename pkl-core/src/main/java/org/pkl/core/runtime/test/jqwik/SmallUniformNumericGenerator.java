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

import java.math.*;
import java.util.*;

class SmallUniformNumericGenerator implements RandomDistribution.RandomNumericGenerator {

  private final int min;
  private final int max;

  SmallUniformNumericGenerator(BigInteger min, BigInteger max) {
    this.min = min.intValueExact();
    this.max = max.intValueExact();
  }

  @Override
  public BigInteger next(Random random) {
    int bound = Math.abs(max - min) + 1;
    int value = random.nextInt(bound >= 0 ? bound : Integer.MAX_VALUE) + min;
    return BigInteger.valueOf(value);
  }
}
