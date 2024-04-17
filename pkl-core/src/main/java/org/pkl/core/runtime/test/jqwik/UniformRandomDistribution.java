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

public class UniformRandomDistribution implements RandomDistribution {

  @Override
  public RandomNumericGenerator createGenerator(
      int genSize, BigInteger min, BigInteger max, BigInteger center) {
    // Small number generation can be faster
    if (isWithinIntegerRange(min, max)) {
      return new SmallUniformNumericGenerator(min, max);
    } else {
      return new BigUniformNumericGenerator(min, max);
    }
  }

  private static boolean isWithinIntegerRange(BigInteger min, BigInteger max) {
    boolean rangeIsSmallerThanIntegerMax =
        max.subtract(min).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0;
    boolean minAndMaxAreWithinInt =
        min.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0
            && max.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0;
    return rangeIsSmallerThanIntegerMax && minAndMaxAreWithinInt;
  }

  @Override
  public String toString() {
    return "UniformDistribution";
  }
}
