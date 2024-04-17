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

class BigUniformNumericGenerator implements RandomDistribution.RandomNumericGenerator {

  private final BigInteger min;
  private final BigInteger max;
  private final BigInteger range;
  private final int bits;

  BigUniformNumericGenerator(BigInteger min, BigInteger max) {
    this.min = min;
    this.max = max;
    this.range = max.subtract(min);
    this.bits = range.bitLength();
  }

  @Override
  public BigInteger next(Random random) {
    while (true) {
      BigInteger rawValue = new BigInteger(bits, random);
      BigInteger value = rawValue.add(min);
      if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
        return value;
      }
    }
  }
}
