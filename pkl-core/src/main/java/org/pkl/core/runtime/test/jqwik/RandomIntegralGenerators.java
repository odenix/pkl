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
import org.pkl.core.runtime.test.jqwik.RandomDistribution.RandomNumericGenerator;

public class RandomIntegralGenerators {

  public static RandomGenerator<BigInteger> bigIntegers(
      int genSize,
      BigInteger min,
      BigInteger max,
      BigInteger shrinkingTarget,
      RandomDistribution distribution) {
    Range<BigInteger> range = Range.of(min, max);

    checkTargetInRange(range, shrinkingTarget);

    if (range.isSingular()) {
      return ignored -> Shrinkable.unshrinkable(range.min);
    }

    RandomNumericGenerator numericGenerator =
        distribution.createGenerator(genSize, range.min, range.max, shrinkingTarget);

    return random -> {
      BigInteger value = numericGenerator.next(random);
      return new ShrinkableBigInteger(value, range, shrinkingTarget);
    };
  }

  private static void checkTargetInRange(Range<BigInteger> range, BigInteger value) {
    if (!range.includes(value)) {
      String message =
          String.format("Shrinking target <%s> is outside allowed range %s", value, range);
      throw new JqwikException(message);
    }
  }

  public static BigInteger defaultShrinkingTarget(Range<BigInteger> range) {
    if (range.includes(BigInteger.ZERO)) {
      return BigInteger.ZERO;
    }
    if (range.max.compareTo(BigInteger.ZERO) < 0) return range.max;
    if (range.min.compareTo(BigInteger.ZERO) > 0) return range.min;
    throw new RuntimeException("This should not be possible");
  }
}
