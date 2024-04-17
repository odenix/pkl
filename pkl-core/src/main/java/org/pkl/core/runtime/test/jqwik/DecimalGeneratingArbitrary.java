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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DecimalGeneratingArbitrary extends TypedCloneable implements Arbitrary<BigDecimal> {

  private static final int DEFAULT_SCALE = 2;

  Range<BigDecimal> range;
  int scale = DEFAULT_SCALE;
  BigDecimal shrinkingTarget;
  RandomDistribution distribution = RandomDistribution.biased();

  private Consumer<EdgeCases.Config<BigDecimal>> edgeCasesConfigurator =
      EdgeCases.Config.noConfig();

  DecimalGeneratingArbitrary(Range<BigDecimal> defaultRange) {
    this.range = defaultRange;
    this.shrinkingTarget = null;
  }

  @Override
  public RandomGenerator<BigDecimal> generator(int genSize) {
    checkRange();
    return RandomDecimalGenerators.bigDecimals(
        genSize, range, scale, distribution, shrinkingTarget());
  }

  private void checkRange() {
    checkScale(range.min);
    checkScale(range.max);
  }

  private void checkScale(final BigDecimal value) {
    try {
      value.setScale(scale);
    } catch (ArithmeticException arithmeticException) {
      String message =
          String.format(
              "Decimal value %s cannot be represented with scale %s.%nYou may want to use a higher scale",
              value, scale);
      throw new JqwikException(message);
    }
  }

  @Override
  public Optional<ExhaustiveGenerator<BigDecimal>> exhaustive(long maxNumberOfSamples) {
    if (range.isSingular()) {
      return ExhaustiveGenerators.choose(Collections.singletonList(range.min), maxNumberOfSamples);
    }
    return Optional.empty();
  }

  @Override
  public EdgeCases<BigDecimal> edgeCases(int maxEdgeCases) {
    Function<Integer, EdgeCases<BigDecimal>> edgeCasesCreator =
        max -> EdgeCasesSupport.fromShrinkables(edgeCaseShrinkables(max));
    DecimalEdgeCasesConfiguration configuration =
        new DecimalEdgeCasesConfiguration(range, scale, shrinkingTarget());
    return configuration.configure(edgeCasesConfigurator, edgeCasesCreator, maxEdgeCases);
  }

  @Override
  public Arbitrary<BigDecimal> edgeCases(Consumer<EdgeCases.Config<BigDecimal>> configurator) {
    DecimalGeneratingArbitrary clone = typedClone();
    clone.edgeCasesConfigurator = configurator;
    return clone;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DecimalGeneratingArbitrary that = (DecimalGeneratingArbitrary) o;

    if (scale != that.scale) return false;
    if (!range.equals(that.range)) return false;
    if (!Objects.equals(shrinkingTarget, that.shrinkingTarget)) return false;
    if (!Objects.equals(distribution, that.distribution)) return false;
    return LambdaSupport.areEqual(edgeCasesConfigurator, that.edgeCasesConfigurator);
  }

  @Override
  public int hashCode() {
    return HashCodeSupport.hash(range, scale, shrinkingTarget);
  }

  private List<Shrinkable<BigDecimal>> edgeCaseShrinkables(int maxEdgeCases) {
    Range<BigInteger> bigIntegerRange = unscaledBigIntegerRange(range, scale);
    return streamRawEdgeCases()
        .filter(aDecimal -> range.includes(aDecimal))
        .map(
            value -> {
              BigInteger bigIntegerValue = unscaledBigInteger(value, scale);
              BigInteger shrinkingTarget = unscaledBigInteger(shrinkingTarget(), scale);
              return new ShrinkableBigInteger(bigIntegerValue, bigIntegerRange, shrinkingTarget);
            })
        .map(
            shrinkableBigInteger ->
                shrinkableBigInteger.map(bigInteger -> scaledBigDecimal(bigInteger, scale)))
        .limit(Math.max(0, maxEdgeCases))
        .collect(Collectors.toList());
  }

  private Stream<BigDecimal> streamRawEdgeCases() {
    BigDecimal smallest = BigDecimal.ONE.movePointLeft(scale);
    BigDecimal minBorder = range.minIncluded ? range.min : range.min.add(smallest);
    BigDecimal maxBorder = range.maxIncluded ? range.max : range.max.subtract(smallest);
    BigDecimal[] literalEdgeCases = {
      BigDecimal.ZERO.movePointLeft(scale),
      BigDecimal.ONE,
      BigDecimal.ONE.negate(),
      smallest,
      smallest.negate(),
      minBorder,
      maxBorder
    };

    if (shrinkingTarget == null) {
      return Arrays.stream(literalEdgeCases);
    } else {
      return Stream.concat(
          Stream.of(
              shrinkingTarget, shrinkingTarget.add(smallest), shrinkingTarget.subtract(smallest)),
          Arrays.stream(literalEdgeCases));
    }
  }

  private BigDecimal shrinkingTarget() {
    if (shrinkingTarget == null) {
      return RandomDecimalGenerators.defaultShrinkingTarget(range, scale);
    } else {
      return shrinkingTarget;
    }
  }
}
