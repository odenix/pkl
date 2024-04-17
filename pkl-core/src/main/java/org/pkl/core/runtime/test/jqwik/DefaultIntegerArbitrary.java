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

public class DefaultIntegerArbitrary extends TypedCloneable implements IntegerArbitrary {

  private static final int DEFAULT_MIN = Integer.MIN_VALUE;
  private static final int DEFAULT_MAX = Integer.MAX_VALUE;

  private IntegralGeneratingArbitrary generatingArbitrary;

  public DefaultIntegerArbitrary() {
    this.generatingArbitrary =
        new IntegralGeneratingArbitrary(
            BigInteger.valueOf(DEFAULT_MIN), BigInteger.valueOf(DEFAULT_MAX));
  }

  @Override
  public RandomGenerator<Integer> generator(int genSize) {
    return generatingArbitrary.generator(genSize).map(BigInteger::intValueExact);
  }

  @Override
  public Optional<ExhaustiveGenerator<Integer>> exhaustive(long maxNumberOfSamples) {
    return generatingArbitrary
        .exhaustive(maxNumberOfSamples)
        .map(generator -> generator.map(BigInteger::intValueExact));
  }

  @Override
  public EdgeCases<Integer> edgeCases(int maxEdgeCases) {
    return EdgeCasesSupport.map(
        generatingArbitrary.edgeCases(maxEdgeCases), BigInteger::intValueExact);
  }

  @Override
  public Arbitrary<Integer> edgeCases(Consumer<EdgeCases.Config<Integer>> configurator) {
    Consumer<EdgeCases.Config<BigInteger>> integralConfigurator =
        new MappedEdgeCasesConsumer<>(
            configurator,
            BigInteger::intValueExact,
            (Function<Integer, BigInteger>) BigInteger::valueOf);
    DefaultIntegerArbitrary clone = typedClone();
    clone.generatingArbitrary =
        (IntegralGeneratingArbitrary) generatingArbitrary.edgeCases(integralConfigurator);
    return clone;
  }

  @Override
  public IntegerArbitrary withDistribution(final RandomDistribution distribution) {
    DefaultIntegerArbitrary clone = typedClone();
    clone.generatingArbitrary.distribution = distribution;
    return clone;
  }

  @Override
  public IntegerArbitrary greaterOrEqual(int min) {
    DefaultIntegerArbitrary clone = typedClone();
    clone.generatingArbitrary.min = BigInteger.valueOf(min);
    return clone;
  }

  @Override
  public IntegerArbitrary lessOrEqual(int max) {
    DefaultIntegerArbitrary clone = typedClone();
    clone.generatingArbitrary.max = BigInteger.valueOf(max);
    return clone;
  }

  @Override
  public IntegerArbitrary shrinkTowards(int target) {
    DefaultIntegerArbitrary clone = typedClone();
    clone.generatingArbitrary.shrinkingTarget = BigInteger.valueOf(target);
    return clone;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DefaultIntegerArbitrary that = (DefaultIntegerArbitrary) o;
    return generatingArbitrary.equals(that.generatingArbitrary);
  }

  @Override
  public int hashCode() {
    return generatingArbitrary.hashCode();
  }
}
