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

public class DefaultIteratorArbitrary<T> extends MultivalueArbitraryBase<T, Iterator<T>>
    implements IteratorArbitrary<T> {

  public DefaultIteratorArbitrary(Arbitrary<T> elementArbitrary) {
    super(elementArbitrary);
  }

  @Override
  protected Iterable<T> toIterable(Iterator<T> streamable) {
    return () -> streamable;
  }

  @Override
  public RandomGenerator<Iterator<T>> generator(int genSize) {
    return createListGenerator(genSize, false).map(List::iterator);
  }

  @Override
  public RandomGenerator<Iterator<T>> generatorWithEmbeddedEdgeCases(int genSize) {
    return createListGenerator(genSize, true).map(List::iterator);
  }

  @Override
  public Optional<ExhaustiveGenerator<Iterator<T>>> exhaustive(long maxNumberOfSamples) {
    return ExhaustiveGenerators.list(
            elementArbitrary, minSize, maxSize(), uniquenessExtractors, maxNumberOfSamples)
        .map(generator -> generator.map(List::iterator));
  }

  @Override
  public EdgeCases<Iterator<T>> edgeCases(int maxEdgeCases) {
    return EdgeCasesSupport.map(
        edgeCases(
            (elements, minimalSize) ->
                new ShrinkableList<>(
                    elements, minimalSize, maxSize(), uniquenessExtractors, elementArbitrary),
            maxEdgeCases),
        List::iterator);
  }

  @Override
  public IteratorArbitrary<T> ofMaxSize(int maxSize) {
    return (IteratorArbitrary<T>) super.ofMaxSize(maxSize);
  }

  @Override
  public IteratorArbitrary<T> ofMinSize(int minSize) {
    return (IteratorArbitrary<T>) super.ofMinSize(minSize);
  }

  @Override
  public IteratorArbitrary<T> withSizeDistribution(RandomDistribution distribution) {
    return (IteratorArbitrary<T>) super.withSizeDistribution(distribution);
  }

  @Override
  public IteratorArbitrary<T> uniqueElements(Function<T, Object> by) {
    FeatureExtractor<T> featureExtractor = by::apply;
    return (IteratorArbitrary<T>) super.uniqueElements(featureExtractor);
  }

  @Override
  public IteratorArbitrary<T> uniqueElements() {
    return (IteratorArbitrary<T>) super.uniqueElements();
  }
}
