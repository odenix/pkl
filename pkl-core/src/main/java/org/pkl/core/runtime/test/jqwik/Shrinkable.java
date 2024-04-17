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
import javax.annotation.*;

public interface Shrinkable<T> extends Comparable<Shrinkable<T>> {

  class ShrinkableFacade {
    private static final ShrinkableFacade implementation = new ShrinkableFacade();

    public <T> Shrinkable<T> unshrinkable(Supplier<T> valueSupplier, ShrinkingDistance distance) {
      return new Unshrinkable<>(valueSupplier, distance);
    }

    public <T, U> Shrinkable<U> map(Shrinkable<T> self, Function<T, U> mapper) {
      return new MappedShrinkable<>(self, mapper);
    }

    public <T> Shrinkable<T> filter(Shrinkable<T> self, Predicate<T> filter) {
      return new FilteredShrinkable<>(self, filter);
    }

    public <T, U> Shrinkable<U> flatMap(
        Shrinkable<T> self, Function<T, Arbitrary<U>> flatMapper, int tries, long randomSeed) {
      return new FlatMappedShrinkable<>(self, flatMapper, tries, randomSeed, false);
    }
  }

  static <T> Shrinkable<T> unshrinkable(@Nullable T value) {
    return unshrinkable(value, ShrinkingDistance.MIN);
  }

  static <T> Shrinkable<T> unshrinkable(@Nullable T value, ShrinkingDistance distance) {
    return ShrinkableFacade.implementation.unshrinkable(() -> value, distance);
  }

  static <T> Shrinkable<T> supplyUnshrinkable(Supplier<T> supplier) {
    return ShrinkableFacade.implementation.unshrinkable(supplier, ShrinkingDistance.MIN);
  }

  /**
   * Create value freshly, so that in case of mutable objects shrinking (and reporting) can rely on
   * untouched values.
   *
   * @return An un-changed instance of the value represented by this shrinkable
   */
  T value();

  /**
   * Create a new and finite stream of smaller or same size shrinkables; size is measured by
   * {@linkplain #distance()}.
   *
   * <p>Same size shrinkables are allowed but they have to iterate towards a single value to prevent
   * endless shrinking. This also means that a shrinkable must never be in its own shrink stream!
   *
   * @return a finite stream of shrinking options
   */
  Stream<Shrinkable<T>> shrink();

  /**
   * To be able to "move" values towards the end of collections while keeping some constraint
   * constant it's necessary to grow a shrinkable by what another has been shrunk. One example is
   * keeping a sum of values and still shrinking to the same resulting list.
   *
   * @param before The other shrinkable before shrinking
   * @param after The other shrinkable after shrinking
   * @return this shrinkable grown by the difference of before and after
   */
  default Optional<Shrinkable<T>> grow(Shrinkable<?> before, Shrinkable<?> after) {
    return Optional.empty();
  }

  /**
   * Grow a shrinkable to allow broader searching in flat mapped shrinkables
   *
   * @return a finite stream of grown values
   */
  default Stream<Shrinkable<T>> grow() {
    return Stream.empty();
  }

  ShrinkingDistance distance();

  /**
   * Sometimes simplifies test writing
   *
   * @return generic version of a shrinkable
   */
  @SuppressWarnings("unchecked")
  default Shrinkable<Object> asGeneric() {
    return (Shrinkable<Object>) this;
  }

  default <U> Shrinkable<U> map(Function<T, U> mapper) {
    return ShrinkableFacade.implementation.map(this, mapper);
  }

  default Shrinkable<T> filter(Predicate<T> filter) {
    return ShrinkableFacade.implementation.filter(this, filter);
  }

  default <U> Shrinkable<U> flatMap(
      Function<T, Arbitrary<U>> flatMapper, int tries, long randomSeed) {
    return ShrinkableFacade.implementation.flatMap(this, flatMapper, tries, randomSeed);
  }

  @SuppressWarnings("unchecked")
  @Override
  default int compareTo(Shrinkable<T> other) {
    int comparison = this.distance().compareTo(other.distance());
    if (comparison == 0) {
      T value = value();
      if (value instanceof Comparable && this.getClass().equals(other.getClass())) {
        return ((Comparable<T>) value).compareTo(other.value());
      }
    }
    return comparison;
  }

  default Shrinkable<T> makeUnshrinkable() {
    return ShrinkableFacade.implementation.unshrinkable(this::value, this.distance());
  }
}
