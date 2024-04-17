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

import static org.pkl.core.runtime.test.jqwik.RandomDecimalGenerators.scaledBigDecimal;
import static org.pkl.core.runtime.test.jqwik.RandomDecimalGenerators.unscaledBigInteger;
import static org.pkl.core.runtime.test.jqwik.RandomDecimalGenerators.unscaledBigIntegerRange;

import java.math.BigDecimal;
import java.math.BigInteger;

class DecimalEdgeCasesConfiguration extends GenericEdgeCasesConfiguration<BigDecimal> {

  private final Range<BigDecimal> range;
  private final int scale;
  private final BigDecimal shrinkingTarget;

  public DecimalEdgeCasesConfiguration(
      Range<BigDecimal> range, int scale, BigDecimal shrinkingTarget) {
    this.range = range;
    this.scale = scale;
    this.shrinkingTarget = shrinkingTarget;
  }

  @Override
  protected void checkEdgeCaseIsValid(BigDecimal edgeCase) {
    if (!range.includes(edgeCase)) {
      String message =
          String.format(
              "Edge case <%s> is outside the arbitrary's allowed range %s", edgeCase, range);
      throw new IllegalArgumentException(message);
    }
  }

  @Override
  protected Shrinkable<BigDecimal> createShrinkable(BigDecimal additionalEdgeCase) {
    Range<BigInteger> bigIntegerRange = unscaledBigIntegerRange(range, scale);
    BigInteger bigIntegerValue = unscaledBigInteger(additionalEdgeCase, scale);
    BigInteger integralShrinkingTarget = unscaledBigInteger(shrinkingTarget, scale);
    return new ShrinkableBigInteger(bigIntegerValue, bigIntegerRange, integralShrinkingTarget)
        .map(bigInteger -> scaledBigDecimal(bigInteger, scale));
  }
}
