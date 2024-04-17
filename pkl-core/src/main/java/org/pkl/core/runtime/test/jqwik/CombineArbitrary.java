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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CombineArbitrary<R> implements Arbitrary<R> {

  private final Function<List<Object>, R> combinator;
  private final List<Arbitrary<Object>> arbitraries;

  @SuppressWarnings("unchecked")
  public CombineArbitrary(Function<List<Object>, R> combinator, Arbitrary<?>... arbitraries) {
    this.combinator = combinator;
    this.arbitraries = Arrays.asList((Arbitrary<Object>[]) arbitraries);
  }

  @Override
  public RandomGenerator<R> generator(int genSize) {
    return combineGenerator(genSize, combinator, arbitraries);
  }

  @Override
  public RandomGenerator<R> generatorWithEmbeddedEdgeCases(int genSize) {
    return combineGeneratorWithEmbeddedEdgeCases(genSize, combinator, arbitraries);
  }

  @Override
  public Optional<ExhaustiveGenerator<R>> exhaustive(long maxNumberOfSamples) {
    return combineExhaustive(arbitraries, combinator, maxNumberOfSamples);
  }

  @Override
  public boolean isGeneratorMemoizable() {
    return isCombinedGeneratorMemoizable(arbitraries);
  }

  @Override
  public EdgeCases<R> edgeCases(int maxEdgeCases) {
    return combineEdgeCases(arbitraries, combinator, maxEdgeCases);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CombineArbitrary<?> that = (CombineArbitrary<?>) o;
    if (!arbitraries.equals(that.arbitraries)) return false;
    return LambdaSupport.areEqual(combinator, that.combinator);
  }

  @Override
  public int hashCode() {
    return HashCodeSupport.hash(arbitraries);
  }

  private boolean isCombinedGeneratorMemoizable(List<Arbitrary<Object>> arbitraries) {
    return arbitraries.stream().allMatch(Arbitrary::isGeneratorMemoizable);
  }

  private RandomGenerator<R> combineGenerator(
      int genSize, Function<List<Object>, R> combineFunction, List<Arbitrary<Object>> arbitraries) {
    List<RandomGenerator<Object>> generators =
        arbitraries.stream().map(a -> a.generator(genSize)).collect(Collectors.toList());
    return random -> {
      List<Shrinkable<Object>> shrinkables = generateShrinkables(generators, random);
      return combineShrinkables(shrinkables, combineFunction);
    };
  }

  private RandomGenerator<R> combineGeneratorWithEmbeddedEdgeCases(
      int genSize, Function<List<Object>, R> combineFunction, List<Arbitrary<Object>> arbitraries) {
    List<RandomGenerator<Object>> generators =
        arbitraries.stream()
            .map(a -> a.generatorWithEmbeddedEdgeCases(genSize))
            .collect(Collectors.toList());
    return random -> {
      List<Shrinkable<Object>> shrinkables = generateShrinkables(generators, random);
      return combineShrinkables(shrinkables, combineFunction);
    };
  }

  private List<Shrinkable<Object>> generateShrinkables(
      List<RandomGenerator<Object>> generators, Random random) {
    List<Shrinkable<Object>> list = new ArrayList<>();
    for (RandomGenerator<Object> generator : generators) {
      list.add(generator.next(random));
    }
    return list;
  }

  private Shrinkable<R> combineShrinkables(
      List<Shrinkable<Object>> shrinkables, Function<List<Object>, R> combineFunction) {
    return new CombinedShrinkable<>(shrinkables, combineFunction);
  }

  private Optional<ExhaustiveGenerator<R>> combineExhaustive(
      List<Arbitrary<Object>> arbitraries,
      Function<List<Object>, R> combineFunction,
      long maxNumberOfSamples) {
    return ExhaustiveGenerators.combine(arbitraries, combineFunction, maxNumberOfSamples);
  }

  private EdgeCases<R> combineEdgeCases(
      final List<Arbitrary<Object>> arbitraries,
      final Function<List<Object>, R> combineFunction,
      int maxEdgeCases) {
    return EdgeCasesSupport.combine(arbitraries, combineFunction, maxEdgeCases);
  }
}
