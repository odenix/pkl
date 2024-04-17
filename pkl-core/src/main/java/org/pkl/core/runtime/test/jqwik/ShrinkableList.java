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

import static org.pkl.core.runtime.test.jqwik.UniquenessChecker.checkUniquenessOfShrinkables;

import java.util.*;
import java.util.stream.*;

public class ShrinkableList<E> extends ShrinkableContainer<List<E>, E> {

  // Only used in tests
  ShrinkableList(List<Shrinkable<E>> elements, int minSize, int maxSize) {
    this(elements, minSize, maxSize, Collections.emptySet(), null);
  }

  public ShrinkableList(
      List<Shrinkable<E>> elements,
      int minSize,
      int maxSize,
      Collection<FeatureExtractor<E>> uniquenessExtractors,
      Arbitrary<E> elementArbitrary) {
    super(elements, minSize, maxSize, uniquenessExtractors, elementArbitrary);
  }

  @Override
  List<E> createValue(List<Shrinkable<E>> shrinkables) {
    // Using loop instead of stream to make stack traces more readable
    List<E> values = new ArrayList<>(shrinkables.size());
    for (Shrinkable<E> shrinkable : shrinkables) {
      values.add(shrinkable.value());
    }
    return values;
  }

  @Override
  Shrinkable<List<E>> createShrinkable(List<Shrinkable<E>> shrunkElements) {
    return new ShrinkableList<>(
        shrunkElements, minSize, maxSize, uniquenessExtractors, elementArbitrary);
  }

  @Override
  public Stream<Shrinkable<List<E>>> shrink() {
    return JqwikStreamSupport.concat(
        super.shrink(), sortElements(), moveIndividualValuesTowardsEnd());
  }

  // TODO: Simplify and clean up
  private Stream<Shrinkable<List<E>>> moveIndividualValuesTowardsEnd() {
    ShrinkingDistance distance = distance();
    return Combinatorics.distinctPairs(elements.size())
        .map(
            pair -> {
              int firstIndex = Math.min(pair.get1(), pair.get2());
              int secondIndex = Math.max(pair.get1(), pair.get2());
              Shrinkable<E> first = elements.get(firstIndex);
              Shrinkable<E> second = elements.get(secondIndex);
              return Tuple.of(firstIndex, first, secondIndex, second);
            })
        .filter(quadruple -> quadruple.get2().compareTo(quadruple.get4()) <= 0)
        .flatMap(
            quadruple -> {
              int firstIndex = quadruple.get1();
              Shrinkable<E> first = quadruple.get2();
              int secondIndex = quadruple.get3();
              Shrinkable<E> second = quadruple.get4();
              return first
                  .shrink()
                  .map(
                      after -> {
                        Optional<Shrinkable<E>> grow = second.grow(first, after);
                        return Tuple.of(after, grow);
                      })
                  .filter(tuple -> tuple.get2().isPresent())
                  .map(
                      tuple -> {
                        List<Shrinkable<E>> pairMove = new ArrayList<>(elements);
                        pairMove.set(firstIndex, tuple.get1());
                        pairMove.set(secondIndex, tuple.get2().get());
                        return pairMove;
                      })
                  .filter(
                      shrinkables ->
                          checkUniquenessOfShrinkables(uniquenessExtractors, shrinkables))
                  .map(this::createShrinkable);
            })
        // In rare cases of nested lists shrinkGrow can increase the distance
        .filter(s -> s.distance().compareTo(distance) <= 0);
  }
}
