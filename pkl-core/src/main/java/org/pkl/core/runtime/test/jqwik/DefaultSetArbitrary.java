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

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class DefaultSetArbitrary<T> extends MultivalueArbitraryBase<T, Set<T>>
    implements SetArbitrary<T> {

  public DefaultSetArbitrary(Arbitrary<T> elementArbitrary) {
    super(elementArbitrary);
    uniquenessExtractors.add(FeatureExtractor.identity());
  }

  @Override
  protected Iterable<T> toIterable(Set<T> streamable) {
    return streamable;
  }

  @Override
  public RandomGenerator<Set<T>> generator(int genSize) {
    return rawGenerator(genSize, false);
  }

  @Override
  public RandomGenerator<Set<T>> generatorWithEmbeddedEdgeCases(int genSize) {
    return rawGenerator(genSize, true);
  }

  private RandomGenerator<Set<T>> rawGenerator(int genSize, boolean withEmbeddedEdgeCases) {
    RandomGenerator<T> elementGenerator =
        elementGenerator(elementArbitrary, genSize, withEmbeddedEdgeCases);
    return RandomGenerators.set(
        elementGenerator,
        minSize,
        maxSize(),
        genSize,
        sizeDistribution,
        uniquenessExtractors,
        elementArbitrary);
  }

  @Override
  public Optional<ExhaustiveGenerator<Set<T>>> exhaustive(long maxNumberOfSamples) {
    return ExhaustiveGenerators.set(
        elementArbitrary, minSize, maxSize(), uniquenessExtractors, maxNumberOfSamples);
  }

  @Override
  public EdgeCases<Set<T>> edgeCases(int maxEdgeCases) {
    return edgeCases(
        (elementList, minSize1) -> {
          Set<Shrinkable<T>> elementSet = new LinkedHashSet<>(elementList);
          return new ShrinkableSet<>(
              elementSet, minSize1, maxSize(), uniquenessExtractors, elementArbitrary);
        },
        maxEdgeCases);
  }

  @Override
  public SetArbitrary<T> ofMaxSize(int maxSize) {
    return (SetArbitrary<T>) super.ofMaxSize(maxSize);
  }

  @Override
  public SetArbitrary<T> ofMinSize(int minSize) {
    return (SetArbitrary<T>) super.ofMinSize(minSize);
  }

  @Override
  public SetArbitrary<T> withSizeDistribution(RandomDistribution distribution) {
    return (SetArbitrary<T>) super.withSizeDistribution(distribution);
  }

  // TODO: Remove duplication with DefaultListArbitrary.mapEach()
  @Override
  public <U> Arbitrary<Set<U>> mapEach(BiFunction<Set<T>, T, U> mapper) {
    return this.map(
        elements ->
            elements.stream()
                .map(e -> mapper.apply(elements, e))
                .collect(CollectorsSupport.toLinkedHashSet()));
  }

  // TODO: Remove duplication with DefaultListArbitrary.flatMapEach()
  @Override
  public <U> Arbitrary<Set<U>> flatMapEach(BiFunction<Set<T>, T, Arbitrary<U>> flatMapper) {
    return this.flatMap(
        elements -> {
          List<Arbitrary<U>> arbitraries =
              elements.stream()
                  .map(e -> flatMapper.apply(elements, e))
                  .collect(Collectors.toList());
          return Combinators.combine(arbitraries).as(LinkedHashSet::new);
        });
  }

  @Override
  public SetArbitrary<T> uniqueElements() {
    return this;
  }

  @Override
  public SetArbitrary<T> uniqueElements(Function<T, Object> by) {
    FeatureExtractor<T> featureExtractor = by::apply;
    return (SetArbitrary<T>) super.uniqueElements(featureExtractor);
  }
}
