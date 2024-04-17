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

import static java.util.Arrays.*;

import java.util.*;
import java.util.ArrayList;
import java.util.function.*;
import java.util.stream.*;
import org.pkl.core.runtime.test.jqwik.Tuple.Tuple2;

public class Combinatorics {

  public static <T> Iterator<List<T>> combine(List<Iterable<T>> listOfIterables) {
    if (listOfIterables.isEmpty()) {
      return emptyListSingleton();
    }
    return new CombinedIterator<>(listOfIterables);
  }

  @SuppressWarnings("unchecked")
  private static <T> Iterator<List<T>> emptyListSingleton() {
    return asList((List<T>) new ArrayList<>()).iterator();
  }

  @SuppressWarnings("unchecked")
  private static <T> Iterator<Set<T>> emptySetSingleton() {
    return asList((Set<T>) new LinkedHashSet<>()).iterator();
  }

  public static <T> Iterator<List<T>> listCombinations(
      Iterable<T> elementIterable, int minSize, int maxSize) {
    List<Iterator<List<T>>> iterators = new ArrayList<>();
    for (int listSize = minSize; listSize <= maxSize; listSize++) {
      iterators.add(listIterator(elementIterable, listSize));
    }
    return concatIterators(iterators);
  }

  private static <T> Iterator<List<T>> listIterator(Iterable<T> elementIterable, int listSize) {
    List<Iterable<T>> listOfIterables = new ArrayList<>();
    for (int i = 0; i < listSize; i++) {
      listOfIterables.add(elementIterable);
    }
    return combine(listOfIterables);
  }

  public static <T> Iterator<Set<T>> setCombinations(
      Iterable<T> elementIterable, int minSize, int maxSize) {
    List<Iterator<Set<T>>> iterators = new ArrayList<>();
    for (int setSize = minSize; setSize <= maxSize; setSize++) {
      Iterator<Set<T>> setIterator = setIterator(elementIterable, setSize);
      iterators.add(setIterator);
    }
    return concatIterators(iterators);
  }

  private static <T> Iterator<Set<T>> setIterator(Iterable<T> elementIterable, int setSize) {
    if (setSize == 0) {
      return emptySetSingleton();
    }
    return new SetIterator<>(elementIterable, setSize);
  }

  public static <T> Iterator<List<T>> listPermutations(List<T> values) {
    if (values.isEmpty()) {
      return emptyListSingleton();
    }
    return new PermutationIterator<>(values);
  }

  public static <T> Iterator<T> concat(List<Iterable<T>> iterables) {
    List<Iterator<T>> iterators =
        iterables.stream().map(Iterable::iterator).collect(Collectors.toList());
    return new ConcatIterator<>(iterators);
  }

  private static <T> Iterator<T> concatIterators(List<Iterator<T>> iterators) {
    return new ConcatIterator<>(iterators);
  }

  public static Stream<Tuple2<Integer, Integer>> distinctPairs(int maxExclusive) {
    if (maxExclusive < 2) {
      return Stream.empty();
    }
    return StreamSupport.stream(new PairSpliterator(maxExclusive), false);
  }

  private static class PairSpliterator implements Spliterator<Tuple2<Integer, Integer>> {
    private final int maxExclusive;

    private int i = 0;
    private int j = 1;

    public PairSpliterator(int maxExclusive) {
      this.maxExclusive = maxExclusive;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Tuple2<Integer, Integer>> action) {
      if (j >= maxExclusive) {
        return false;
      }
      action.accept(Tuple.of(i, j));
      j += 1;
      if (j >= maxExclusive) {
        i += 1;
        j = i + 1;
      }
      return true;
    }

    @Override
    public Spliterator<Tuple2<Integer, Integer>> trySplit() {
      return null;
    }

    @Override
    public long estimateSize() {
      return (long) maxExclusive * (maxExclusive - 1) / 2;
    }

    @Override
    public int characteristics() {
      return Spliterator.DISTINCT
          | Spliterator.ORDERED
          | Spliterator.SIZED
          | Spliterator.NONNULL
          | Spliterator.IMMUTABLE;
    }
  }
}
