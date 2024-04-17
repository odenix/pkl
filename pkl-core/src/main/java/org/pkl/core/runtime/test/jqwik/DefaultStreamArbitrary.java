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

public class DefaultStreamArbitrary<T> extends MultivalueArbitraryBase<T, Stream<T>>
    implements StreamArbitrary<T> {

  public DefaultStreamArbitrary(Arbitrary<T> elementArbitrary) {
    super(elementArbitrary);
  }

  @Override
  protected Iterable<T> toIterable(Stream<T> streamable) {
    return streamable::iterator;
  }

  @Override
  public RandomGenerator<Stream<T>> generator(int genSize) {
    return createListGenerator(genSize, false).map(ReportableStream::new);
  }

  @Override
  public RandomGenerator<Stream<T>> generatorWithEmbeddedEdgeCases(int genSize) {
    return createListGenerator(genSize, true).map(ReportableStream::new);
  }

  @Override
  public Optional<ExhaustiveGenerator<Stream<T>>> exhaustive(long maxNumberOfSamples) {
    return ExhaustiveGenerators.list(
            elementArbitrary, minSize, maxSize(), uniquenessExtractors, maxNumberOfSamples)
        .map(generator -> generator.map(ReportableStream::new));
  }

  @Override
  public EdgeCases<Stream<T>> edgeCases(int maxEdgeCases) {
    return EdgeCasesSupport.map(
        edgeCases(
            (elements, minSize1) ->
                new ShrinkableList<>(
                    elements, minSize1, maxSize(), uniquenessExtractors, elementArbitrary),
            maxEdgeCases),
        ReportableStream::new);
  }

  @Override
  public StreamArbitrary<T> ofMaxSize(int maxSize) {
    return (StreamArbitrary<T>) super.ofMaxSize(maxSize);
  }

  @Override
  public StreamArbitrary<T> ofMinSize(int minSize) {
    return (StreamArbitrary<T>) super.ofMinSize(minSize);
  }

  @Override
  public StreamArbitrary<T> withSizeDistribution(RandomDistribution distribution) {
    return (StreamArbitrary<T>) super.withSizeDistribution(distribution);
  }

  @Override
  public StreamArbitrary<T> uniqueElements(Function<T, Object> by) {
    FeatureExtractor<T> featureExtractor = by::apply;
    return (StreamArbitrary<T>) super.uniqueElements(featureExtractor);
  }

  @Override
  public StreamArbitrary<T> uniqueElements() {
    return (StreamArbitrary<T>) super.uniqueElements();
  }
}
