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

public class BigIntegerShrinker {

  private final BigInteger shrinkingTarget;

  public BigIntegerShrinker(BigInteger shrinkingTarget) {
    this.shrinkingTarget = shrinkingTarget;
  }

  public Stream<BigInteger> shrink(BigInteger value) {
    Set<BigInteger> candidates = new LinkedHashSet<>();
    BigInteger lower = shrinkingTarget.min(value);
    BigInteger higher = shrinkingTarget.max(value);
    addFibbonaci(candidates, lower, BigInteger.valueOf(0), BigInteger.valueOf(1), higher);
    subFibbonaci(candidates, higher, BigInteger.valueOf(0), BigInteger.valueOf(1), lower);
    candidates.add(shrinkingTarget);
    candidates.remove(value);
    return candidates.stream();
  }

  private void subFibbonaci(
      Set<BigInteger> candidates,
      BigInteger target,
      BigInteger butLast,
      BigInteger last,
      BigInteger border) {
    while (true) {
      BigInteger step = butLast.add(last);
      BigInteger candidate = target.subtract(step);
      if (candidate.compareTo(border) <= 0) {
        break;
      }
      candidates.add(candidate);
      butLast = last;
      last = step;
    }
  }

  private void addFibbonaci(
      Set<BigInteger> candidates,
      BigInteger target,
      BigInteger butLast,
      BigInteger last,
      BigInteger border) {
    while (true) {
      BigInteger step = butLast.add(last);
      BigInteger candidate = target.add(step);
      if (candidate.compareTo(border) >= 0) {
        break;
      }
      candidates.add(candidate);
      butLast = last;
      last = step;
    }
  }
}
