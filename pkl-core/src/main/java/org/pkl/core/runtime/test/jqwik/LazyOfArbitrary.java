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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.pkl.core.runtime.test.jqwik.Tuple.Tuple1;
import org.pkl.core.runtime.test.jqwik.Tuple.Tuple2;

public class LazyOfArbitrary<T> implements Arbitrary<T> {

  // Cached arbitraries only have to survive one property
  private static Store<Map<Integer, LazyOfArbitrary<?>>> arbitrariesStore() {
    try {
      return Store.getOrCreate(
          Tuple.of(LazyOfShrinkable.class, "arbitraries"), Lifespan.PROPERTY, LinkedHashMap::new);
    } catch (OutsideJqwikException outsideJqwikException) {
      return Store.free(LinkedHashMap::new);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> Arbitrary<T> of(int hashIdentifier, List<Supplier<Arbitrary<T>>> suppliers) {
    // It's important for good shrinking to work that the same arbitrary usage is handled by the
    // same arbitrary instance
    LazyOfArbitrary<?> arbitrary =
        arbitrariesStore()
            .get()
            .computeIfAbsent(hashIdentifier, ignore -> new LazyOfArbitrary<>(suppliers));
    return (Arbitrary<T>) arbitrary;
  }

  private final List<Supplier<Arbitrary<T>>> suppliers;

  private final Deque<Set<LazyOfShrinkable<T>>> generatedParts = new ArrayDeque<>();

  // Remember generators during the same try. That way generators with state (e.g. unique()) work as
  // expected
  private final Store<Map<Integer, RandomGenerator<T>>> generators = createGeneratorsStore();

  private Store<Map<Integer, RandomGenerator<T>>> createGeneratorsStore() {
    try {
      return Store.getOrCreate(Tuple.of(this, "generators"), Lifespan.TRY, LinkedHashMap::new);
    } catch (OutsideJqwikException outsideJqwikException) {
      return Store.free(LinkedHashMap::new);
    }
  }

  public LazyOfArbitrary(List<Supplier<Arbitrary<T>>> suppliers) {
    this.suppliers = suppliers;
  }

  @Override
  public RandomGenerator<T> generator(int genSize) {
    return random -> {
      int index = random.nextInt(suppliers.size());
      long seed = random.nextLong();

      Tuple2<Shrinkable<T>, Set<LazyOfShrinkable<T>>> shrinkableAndParts =
          generateCurrent(genSize, index, seed);
      return createShrinkable(shrinkableAndParts, genSize, seed, Collections.singleton(index));
    };
  }

  private LazyOfShrinkable<T> createShrinkable(
      Tuple2<Shrinkable<T>, Set<LazyOfShrinkable<T>>> shrinkableAndParts,
      int genSize,
      long seed,
      Set<Integer> usedIndices) {
    Shrinkable<T> shrinkable = shrinkableAndParts.get1();
    Set<LazyOfShrinkable<T>> parts = shrinkableAndParts.get2();
    LazyOfShrinkable<T> lazyOfShrinkable =
        new LazyOfShrinkable<>(
            shrinkable,
            depth(parts),
            parts,
            (LazyOfShrinkable<T> lazyOf) -> shrink(lazyOf, genSize, seed, usedIndices));
    addGenerated(lazyOfShrinkable);
    return lazyOfShrinkable;
  }

  private void addGenerated(LazyOfShrinkable<T> lazyOfShrinkable) {
    if (peekGenerated() != null) {
      peekGenerated().add(lazyOfShrinkable);
    }
  }

  private Set<LazyOfShrinkable<T>> peekGenerated() {
    return generatedParts.peekFirst();
  }

  private void pushGeneratedLevel() {
    generatedParts.addFirst(new LinkedHashSet<>());
  }

  private void popGeneratedLevel() {
    generatedParts.removeFirst();
  }

  private int depth(Set<LazyOfShrinkable<T>> parts) {
    return parts.stream().mapToInt(p -> p.depth).map(depth -> depth + 1).max().orElse(0);
  }

  private Tuple2<Shrinkable<T>, Set<LazyOfShrinkable<T>>> generateCurrent(
      int genSize, int index, long seed) {
    try {
      pushGeneratedLevel();
      return Tuple.of(
          getGenerator(index, genSize).next(SourceOfRandomness.newRandom(seed)), peekGenerated());
    } finally {
      // To clean up even if there's an exception during value generation
      popGeneratedLevel();
    }
  }

  private RandomGenerator<T> getGenerator(int index, int genSize) {
    if (generators.get().get(index) == null) {
      RandomGenerator<T> generator = suppliers.get(index).get().generator(genSize);
      generators.get().put(index, generator);
    }
    return generators.get().get(index);
  }

  private Stream<Shrinkable<T>> shrink(
      LazyOfShrinkable<T> lazyOf, int genSize, long seed, Set<Integer> usedIndexes) {
    return JqwikStreamSupport.concat(
        shrinkToParts(lazyOf),
        shrinkCurrent(lazyOf, genSize, seed, usedIndexes),
        shrinkToAlternatives(lazyOf.current, genSize, seed, usedIndexes)
        // I don't have an example to show that this adds shrinking quality:
        // shrinkToAlternativesAndGrow(lazyOf.current, genSize, seed, usedIndexes)
        );
  }

  private Stream<Shrinkable<T>> shrinkCurrent(
      LazyOfShrinkable<T> lazyOf, int genSize, long seed, Set<Integer> usedIndexes) {
    return lazyOf
        .current
        .shrink()
        .map(
            shrinkable ->
                new LazyOfShrinkable<>(
                    shrinkable,
                    lazyOf.depth,
                    Collections.emptySet(),
                    (LazyOfShrinkable<T> lazy) -> shrink(lazy, genSize, seed, usedIndexes)));
  }

  private Stream<Shrinkable<T>> shrinkToParts(LazyOfShrinkable<T> lazyOf) {
    return JqwikStreamSupport.concat(
        lazyOf.parts.stream().flatMap(this::shrinkToParts), lazyOf.parts.stream().map(s -> s));
  }

  private Stream<Shrinkable<T>> shrinkToAlternatives(
      Shrinkable<T> current, int genSize, long seed, Set<Integer> usedIndexes) {
    ShrinkingDistance distance = current.distance();
    Set<Integer> newUsedIndexes = new LinkedHashSet<>(usedIndexes);
    return IntStream.range(0, suppliers.size())
        .filter(index -> !usedIndexes.contains(index))
        .peek(newUsedIndexes::add)
        .mapToObj(index -> generateCurrent(genSize, index, seed))
        .filter(shrinkableAndParts -> shrinkableAndParts.get1().distance().compareTo(distance) < 0)
        .map(
            shrinkableAndParts ->
                createShrinkable(shrinkableAndParts, genSize, seed, newUsedIndexes));
  }

  // Currently disabled since I'm not sure if it provides additional value
  @SuppressWarnings("unused")
  private Stream<Shrinkable<T>> shrinkToAlternativesAndGrow(
      Shrinkable<T> current, int genSize, long seed, Set<Integer> usedIndexes) {
    ShrinkingDistance distance = current.distance();
    Set<Integer> newUsedIndexes = new LinkedHashSet<>(usedIndexes);
    return IntStream.range(0, suppliers.size())
        .filter(index -> !usedIndexes.contains(index))
        .peek(newUsedIndexes::add)
        .mapToObj(index -> generateCurrent(genSize, index, seed))
        .map(Tuple1::get1)
        .filter(tShrinkable -> tShrinkable.distance().compareTo(distance) < 0)
        .flatMap(Shrinkable::grow)
        .filter(shrinkable -> shrinkable.distance().compareTo(distance) < 0)
        .map(
            grownShrinkable ->
                createShrinkable(
                    Tuple.of(grownShrinkable, Collections.emptySet()),
                    genSize,
                    seed,
                    newUsedIndexes));
  }

  @Override
  public EdgeCases<T> edgeCases(int maxEdgeCases) {
    return EdgeCases.none();
  }
}
