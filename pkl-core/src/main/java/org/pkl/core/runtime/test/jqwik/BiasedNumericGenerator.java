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

class BiasedNumericGenerator implements RandomNumericGenerator {

  private final RandomNumericGenerator partitionedGenerator;

  BiasedNumericGenerator(int genSize, BigInteger min, BigInteger max, BigInteger center) {
    List<BigInteger> partitionPoints =
        BiasedPartitionPointsCalculator.calculatePartitionPoints(genSize, min, max, center);
    Range<BigInteger> range = Range.of(min, max);
    partitionedGenerator = partitionedGenerator(range, partitionPoints);
  }

  @Override
  public BigInteger next(Random random) {
    return partitionedGenerator.next(random);
  }

  private RandomNumericGenerator partitionedGenerator(
      Range<BigInteger> range, List<BigInteger> partitionPoints) {
    if (partitionPoints.isEmpty()) {
      return createUniformGenerator(range.min, range.max);
    }
    List<RandomNumericGenerator> generators = createPartitions(range, partitionPoints);
    return random -> generators.get(random.nextInt(generators.size())).next(random);
  }

  private List<RandomNumericGenerator> createPartitions(
      Range<BigInteger> range, List<BigInteger> partitionPoints) {
    List<RandomNumericGenerator> partitions = new ArrayList<>();
    Collections.sort(partitionPoints);
    BigInteger lower = range.min;
    for (BigInteger partitionPoint : partitionPoints) {
      BigInteger upper = partitionPoint;
      if (upper.compareTo(lower) <= 0) {
        continue;
      }
      if (upper.compareTo(range.max) >= 0) {
        break;
      }
      partitions.add(createUniformGenerator(lower, upper.subtract(BigInteger.ONE)));
      lower = upper;
    }
    partitions.add(createUniformGenerator(lower, range.max));
    return partitions;
  }

  private RandomNumericGenerator createUniformGenerator(
      BigInteger minGenerate, BigInteger maxGenerate) {
    int ignoredGenSize = 1000;
    BigInteger ignoredCenter = minGenerate;
    return RandomDistribution.uniform()
        .createGenerator(ignoredGenSize, minGenerate, maxGenerate, ignoredCenter);
  }
}
