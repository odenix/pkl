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

class IntegralEdgeCasesConfiguration extends GenericEdgeCasesConfiguration<BigInteger> {

  private final Range<BigInteger> range;
  private final BigInteger shrinkingTarget;

  public IntegralEdgeCasesConfiguration(Range<BigInteger> range, BigInteger shrinkingTarget) {
    this.range = range;
    this.shrinkingTarget = shrinkingTarget;
  }

  @Override
  protected void checkEdgeCaseIsValid(BigInteger edgeCase) {
    if (!range.includes(edgeCase)) {
      String message =
          String.format(
              "Edge case <%s> is outside the arbitrary's allowed range %s", edgeCase, range);
      throw new IllegalArgumentException(message);
    }
  }

  @Override
  protected Shrinkable<BigInteger> createShrinkable(BigInteger additionalEdgeCase) {
    return new ShrinkableBigInteger(additionalEdgeCase, range, shrinkingTarget);
  }
}
