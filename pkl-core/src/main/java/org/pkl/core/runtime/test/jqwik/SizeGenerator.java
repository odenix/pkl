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
import java.util.function.*;

class SizeGenerator {

  private SizeGenerator() {}

  static Function<Random, Integer> create(
      int minSize, int maxSize, int genSize, RandomDistribution distribution) {
    if (distribution != null) {
      return sizeGeneratorWithDistribution(minSize, maxSize, genSize, distribution);
    }
    return sizeGeneratorWithCutoff(minSize, maxSize, genSize);
  }

  private static Function<Random, Integer> sizeGeneratorWithDistribution(
      int minSize, int maxSize, int genSize, RandomDistribution distribution) {
    RandomDistribution.RandomNumericGenerator generator =
        distribution.createGenerator(
            genSize,
            BigInteger.valueOf(minSize),
            BigInteger.valueOf(maxSize),
            BigInteger.valueOf(minSize));
    return random -> generator.next(random).intValueExact();
  }

  private static Function<Random, Integer> sizeGeneratorWithCutoff(
      int minSize, int maxSize, int genSize) {
    int cutoffSize = cutoffSize(minSize, maxSize, genSize);
    if (cutoffSize >= maxSize) return random -> randomSize(random, minSize, maxSize);
    // Choose size below cutoffSize with probability of 0.9
    double maxSizeProbability = Math.min(0.02, 1.0 / (genSize / 10.0));
    double cutoffProbability = 0.1;
    return random -> {
      if (random.nextDouble() <= maxSizeProbability) {
        return maxSize;
      } else if (random.nextDouble() <= (cutoffProbability + maxSizeProbability)) {
        return randomSize(random, cutoffSize + 1, maxSize);
      } else {
        return randomSize(random, minSize, cutoffSize);
      }
    };
  }

  private static int cutoffSize(int minSize, int maxSize, int genSize) {
    int range = maxSize - minSize;
    int offset = (int) Math.max(Math.round(Math.sqrt(genSize)), 10);
    if (range <= offset) return maxSize;
    return Math.min(offset + minSize, maxSize);
  }

  private static int randomSize(Random random, int minSize, int maxSize) {
    int range = maxSize - minSize;
    return random.nextInt(range + 1) + minSize;
  }
}
