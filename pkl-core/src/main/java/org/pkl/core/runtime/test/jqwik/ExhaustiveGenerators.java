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

import static org.pkl.core.runtime.test.jqwik.UniquenessChecker.checkUniquenessOfValues;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class ExhaustiveGenerators {

  public static <T> Optional<ExhaustiveGenerator<T>> create(
      Supplier<T> supplier, long maxNumberOfSamples) {
    return fromIterable(() -> new SupplierIterator<>(supplier), 1, maxNumberOfSamples);
  }

  public static <T> Optional<ExhaustiveGenerator<T>> choose(
      List<T> values, long maxNumberOfSamples) {
    return fromIterable(values, values.size(), maxNumberOfSamples);
  }

  public static Optional<ExhaustiveGenerator<Character>> choose(
      char[] characters, long maxNumberOfSamples) {
    List<Character> validCharacters = new ArrayList<>(characters.length);
    for (char character : characters) {
      validCharacters.add(character);
    }
    return choose(validCharacters, maxNumberOfSamples);
  }

  public static <T> Optional<ExhaustiveGenerator<T>> fromIterable(
      Iterable<T> iterator, long maxCount, long maxNumberOfSamples) {
    if (maxCount > maxNumberOfSamples) {
      return Optional.empty();
    }
    return Optional.of(new IterableBasedExhaustiveGenerator<>(iterator, maxCount));
  }

  public static <T> Optional<ExhaustiveGenerator<List<T>>> list(
      Arbitrary<T> elementArbitrary,
      int minSize,
      int maxSize,
      Collection<FeatureExtractor<T>> uniquenessExtractors,
      long maxNumberOfSamples) {
    Optional<Long> optionalMaxCount =
        ListExhaustiveGenerator.calculateMaxCount(
            elementArbitrary, minSize, maxSize, maxNumberOfSamples);
    return optionalMaxCount.map(
        maxCount -> {
          ListExhaustiveGenerator<T> exhaustiveGenerator =
              new ListExhaustiveGenerator<>(elementArbitrary, maxCount, minSize, maxSize);
          return exhaustiveGenerator.filter(
              l -> checkUniquenessOfValues(uniquenessExtractors, l), 10000);
        });
  }

  public static Optional<ExhaustiveGenerator<String>> strings(
      Arbitrary<Character> characterArbitrary,
      int minLength,
      int maxLength,
      long maxNumberOfSamples,
      boolean uniqueChars) {
    Set<FeatureExtractor<Character>> featureExtractors =
        uniqueChars ? Collections.singleton(FeatureExtractor.identity()) : Collections.emptySet();
    return list(characterArbitrary, minLength, maxLength, featureExtractors, maxNumberOfSamples)
        .map(
            listGenerator ->
                listGenerator.map(
                    listOfChars ->
                        listOfChars.stream().map(String::valueOf).collect(Collectors.joining())));
  }

  public static <T> Optional<ExhaustiveGenerator<Set<T>>> set(
      Arbitrary<T> elementArbitrary,
      int minSize,
      int maxSize,
      Collection<FeatureExtractor<T>> featureExtractors,
      long maxNumberOfSamples) {
    Optional<Long> optionalMaxCount =
        SetExhaustiveGenerator.calculateMaxCount(
            elementArbitrary, minSize, maxSize, maxNumberOfSamples);
    return optionalMaxCount.map(
        maxCount ->
            new SetExhaustiveGenerator<>(elementArbitrary, maxCount, minSize, maxSize)
                .filter(s -> checkUniquenessOfValues(featureExtractors, s), 10000));
  }

  public static <R> Optional<ExhaustiveGenerator<R>> combine(
      List<Arbitrary<Object>> arbitraries,
      Function<List<Object>, R> combinator,
      long maxNumberOfSamples) {
    Optional<Long> optionalMaxCount =
        CombinedExhaustiveGenerator.calculateMaxCount(arbitraries, maxNumberOfSamples);
    return optionalMaxCount.map(
        maxCount -> new CombinedExhaustiveGenerator<>(maxCount, arbitraries, combinator));
  }

  public static <T> Optional<ExhaustiveGenerator<List<T>>> shuffle(
      List<T> values, long maxNumberOfSamples) {
    Optional<Long> optionalMaxCount =
        PermutationExhaustiveGenerator.calculateMaxCount(values, maxNumberOfSamples);
    return optionalMaxCount.map(maxCount -> new PermutationExhaustiveGenerator<>(values, maxCount));
  }

  public static <U, T> Optional<ExhaustiveGenerator<U>> flatMap(
      ExhaustiveGenerator<T> base, Function<T, Arbitrary<U>> mapper, long maxNumberOfSamples) {
    Optional<Long> optionalMaxCount =
        FlatMappedExhaustiveGenerator.calculateMaxCounts(base, mapper, maxNumberOfSamples);
    return optionalMaxCount.map(
        maxCount -> new FlatMappedExhaustiveGenerator<>(base, maxCount, mapper));
  }

  private static class SupplierIterator<T> implements Iterator<T> {

    private final Supplier<T> supplier;
    private volatile boolean generated = false;

    private SupplierIterator(Supplier<T> supplier) {
      this.supplier = supplier;
    }

    @Override
    public boolean hasNext() {
      return !generated;
    }

    @Override
    public T next() {
      if (generated) {
        throw new NoSuchElementException();
      }
      generated = true;
      return supplier.get();
    }
  }
}
