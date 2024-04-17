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

public class CharacterRangeArbitrary implements Arbitrary<Character> {
  private final char min;
  private final char max;

  public CharacterRangeArbitrary(char min, char max) {
    this.min = min;
    this.max = max;
  }

  @Override
  public RandomGenerator<Character> generator(int genSize) {
    return RandomGenerators.chars(min, max);
  }

  private List<Shrinkable<Character>> listOfEdgeCases(int maxEdgeCases) {
    Stream<Character> edgeCases = Stream.of(min, max, ' ').filter(c -> c >= min && c <= max);
    return edgeCases
        .map(
            aCharacter ->
                new ShrinkableBigInteger(
                    BigInteger.valueOf((int) aCharacter),
                    Range.of(BigInteger.valueOf(this.min), BigInteger.valueOf(max)),
                    BigInteger.valueOf(min)))
        .map(shrinkableBigInteger -> shrinkableBigInteger.map(BigInteger::intValueExact))
        .map(shrinkableInteger -> shrinkableInteger.map(anInt -> ((char) (int) anInt)))
        .limit(Math.max(0, maxEdgeCases))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ExhaustiveGenerator<Character>> exhaustive(long maxNumberOfSamples) {
    long maxCount = max + 1 - min;
    return ExhaustiveGenerators.fromIterable(
            () -> IntStream.range(min, max + 1).iterator(), maxCount, maxNumberOfSamples)
        .map(optionalGenerator -> optionalGenerator.map(anInt -> (char) (int) anInt));
  }

  @Override
  public EdgeCases<Character> edgeCases(int maxEdgeCases) {
    return EdgeCasesSupport.fromShrinkables(listOfEdgeCases(maxEdgeCases));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CharacterRangeArbitrary that = (CharacterRangeArbitrary) o;

    if (min != that.min) return false;
    return max == that.max;
  }

  @Override
  public int hashCode() {
    return HashCodeSupport.hash(min, max);
  }
}
