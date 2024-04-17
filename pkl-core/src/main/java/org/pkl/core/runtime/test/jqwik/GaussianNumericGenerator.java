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
import org.pkl.core.runtime.test.jqwik.RandomDistribution.RandomNumericGenerator;

public class GaussianNumericGenerator implements RandomNumericGenerator {
  private final double borderSigma;
  private final BigInteger min;
  private final BigInteger max;
  private final BigInteger center;
  private final BigInteger leftRange;
  private final BigInteger rightRange;

  public GaussianNumericGenerator(
      double borderSigma, BigInteger min, BigInteger max, BigInteger center) {
    this.borderSigma = borderSigma;
    this.min = min;
    this.max = max;
    this.center = center;
    this.leftRange = center.subtract(min).abs();
    this.rightRange = center.subtract(max).abs();
  }

  @Override
  public BigInteger next(Random random) {
    while (true) {
      double gaussianFactor = random.nextGaussian() / borderSigma;
      BigInteger value = center;
      if (gaussianFactor < 0.0 && leftRange.compareTo(BigInteger.ZERO) > 0) {
        BigDecimal bigDecimalLeft =
            new BigDecimal(leftRange).multiply(BigDecimal.valueOf(gaussianFactor).abs());
        value = center.subtract(bigDecimalLeft.toBigInteger());
      }
      if (gaussianFactor > 0.0 && rightRange.compareTo(BigInteger.ZERO) > 0) {
        BigDecimal bigDecimalRight =
            new BigDecimal(rightRange).multiply(BigDecimal.valueOf(gaussianFactor).abs());
        value = center.add(bigDecimalRight.toBigInteger());
      }
      if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
        return value;
      }
    }
  }
}
