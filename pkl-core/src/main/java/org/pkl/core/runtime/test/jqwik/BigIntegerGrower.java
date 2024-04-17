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
import java.util.stream.*;

class BigIntegerGrower {

  Optional<Shrinkable<BigInteger>> grow(
      BigInteger value,
      Range<BigInteger> range,
      BigInteger shrinkingTarget,
      Shrinkable<?> before,
      Shrinkable<?> after) {
    Object beforeValue = before.value();
    Object afterValue = after.value();
    BigInteger diff = calculateDiff(beforeValue, afterValue, value);
    if (diff.compareTo(BigInteger.ZERO) != 0) {
      BigInteger grownValue = value.add(diff);
      if (sameSign(shrinkingTarget.subtract(value), shrinkingTarget.subtract(grownValue))
          && range.includes(grownValue)) {
        return Optional.of(new ShrinkableBigInteger(grownValue, range, shrinkingTarget));
      }
    }
    return Optional.empty();
  }

  private BigInteger calculateDiff(Object beforeValue, Object afterValue, BigInteger current) {
    BigInteger before;
    BigInteger after;
    if (beforeValue instanceof BigInteger && afterValue instanceof BigInteger) {
      before = (BigInteger) beforeValue;
      after = (BigInteger) afterValue;
    } else {
      before = BigInteger.valueOf(toLong(beforeValue));
      after = BigInteger.valueOf(toLong(afterValue));
    }
    if (sameSign(before, current)) {
      return before.subtract(after);
    } else {
      return after.subtract(before);
    }
  }

  private boolean sameSign(BigInteger first, BigInteger second) {
    return Math.abs(first.signum() - second.signum()) <= 1;
  }

  private long toLong(Object value) {
    if (value instanceof Long) {
      return (Long) value;
    }
    if (value instanceof Integer) {
      return ((Integer) value).longValue();
    }
    if (value instanceof Short) {
      return ((Short) value).longValue();
    }
    if (value instanceof Byte) {
      return ((Byte) value).longValue();
    }
    return 0L;
  }

  public Stream<Shrinkable<BigInteger>> grow(
      BigInteger value, Range<BigInteger> range, BigInteger shrinkingTarget) {
    if (value.compareTo(shrinkingTarget) < 0) {
      return growLeft(value, range, shrinkingTarget);
    } else {
      return growRight(value, range, shrinkingTarget);
    }
  }

  private Stream<Shrinkable<BigInteger>> growRight(
      BigInteger value, Range<BigInteger> range, BigInteger shrinkingTarget) {
    return Stream.of(
            range.max,
            value.add(range.max.subtract(value).divide(BigInteger.valueOf(2))),
            value.add(BigInteger.TEN),
            value.add(BigInteger.ONE))
        .filter(grownValue -> grownValue.compareTo(value) > 0)
        .filter(range::includes)
        .distinct()
        .map(grown -> new ShrinkableBigInteger(grown, range, shrinkingTarget));
  }

  private Stream<Shrinkable<BigInteger>> growLeft(
      BigInteger value, Range<BigInteger> range, BigInteger shrinkingTarget) {
    return Stream.of(
            range.min,
            value.subtract(value.subtract(range.min).divide(BigInteger.valueOf(2))),
            value.subtract(BigInteger.TEN),
            value.subtract(BigInteger.ONE))
        .filter(grownValue -> grownValue.compareTo(value) < 0)
        .filter(range::includes)
        .distinct()
        .map(grown -> new ShrinkableBigInteger(grown, range, shrinkingTarget));
  }
}
