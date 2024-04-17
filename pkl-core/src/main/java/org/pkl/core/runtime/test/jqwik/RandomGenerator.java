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

public interface RandomGenerator<T> {

  class RandomGeneratorFacade {
    private static final RandomGeneratorFacade implementation;

    static {
      implementation = new RandomGeneratorFacade();
    }

    public <T, U> Shrinkable<U> flatMap(
        Shrinkable<T> self, Function<T, RandomGenerator<U>> mapper, long nextLong) {
      return new FlatMappedShrinkable<>(self, mapper, nextLong);
    }

    public <T, U> Shrinkable<U> flatMap(
        Shrinkable<T> self,
        Function<T, Arbitrary<U>> mapper,
        int genSize,
        long nextLong,
        boolean withEmbeddedEdgeCases) {
      return new FlatMappedShrinkable<>(self, mapper, genSize, nextLong, withEmbeddedEdgeCases);
    }

    public <T> RandomGenerator<T> filter(
        RandomGenerator<T> self, Predicate<T> filterPredicate, int maxMisses) {
      return new FilteredGenerator<>(self, filterPredicate, maxMisses);
    }

    public <T> RandomGenerator<T> withEdgeCases(
        RandomGenerator<T> self, int genSize, EdgeCases<T> edgeCases) {
      return RandomGenerators.withEdgeCases(self, genSize, edgeCases);
    }

    public <T> RandomGenerator<List<T>> collect(RandomGenerator<T> self, Predicate<List<T>> until) {
      return new CollectGenerator<>(self, until);
    }

    public <T> RandomGenerator<T> injectDuplicates(
        RandomGenerator<T> self, double duplicateProbability) {
      return new InjectDuplicatesGenerator<>(self, duplicateProbability);
    }

    public <T> RandomGenerator<T> ignoreExceptions(
        RandomGenerator<T> self, Class<? extends Throwable>[] exceptionTypes, int maxThrows) {
      return new IgnoreExceptionGenerator<>(self, exceptionTypes, maxThrows);
    }
  }

  /**
   * @param random the source of randomness. Injected by jqwik itself.
   * @return the next generated value wrapped within the Shrinkable interface. The method must
   *     ALWAYS return a next value.
   */
  Shrinkable<T> next(Random random);

  default <U> RandomGenerator<U> map(Function<T, U> mapper) {
    return this.mapShrinkable(s -> s.map(mapper));
  }

  default <U> RandomGenerator<U> mapShrinkable(Function<Shrinkable<T>, Shrinkable<U>> mapper) {
    return random -> {
      Shrinkable<T> tShrinkable = RandomGenerator.this.next(random);
      return mapper.apply(tShrinkable);
    };
  }

  default <U> RandomGenerator<U> flatMap(Function<T, RandomGenerator<U>> mapper) {
    return random -> {
      Shrinkable<T> wrappedShrinkable = RandomGenerator.this.next(random);
      return RandomGeneratorFacade.implementation.flatMap(
          wrappedShrinkable, mapper, random.nextLong());
    };
  }

  default <U> RandomGenerator<U> flatMap(
      Function<T, Arbitrary<U>> mapper, int genSize, boolean withEmbeddedEdgeCases) {
    return random -> {
      Shrinkable<T> wrappedShrinkable = RandomGenerator.this.next(random);
      return RandomGeneratorFacade.implementation.flatMap(
          wrappedShrinkable, mapper, genSize, random.nextLong(), withEmbeddedEdgeCases);
    };
  }

  default RandomGenerator<T> filter(Predicate<T> filterPredicate, int maxMisses) {
    return RandomGeneratorFacade.implementation.filter(this, filterPredicate, maxMisses);
  }

  default RandomGenerator<T> withEdgeCases(int genSize, EdgeCases<T> edgeCases) {
    return RandomGeneratorFacade.implementation.withEdgeCases(this, genSize, edgeCases);
  }

  default Stream<Shrinkable<T>> stream(Random random) {
    return Stream.generate(() -> this.next(random));
  }

  default RandomGenerator<List<T>> collect(Predicate<List<T>> until) {
    return RandomGeneratorFacade.implementation.collect(this, until);
  }

  default RandomGenerator<T> injectDuplicates(double duplicateProbability) {
    return RandomGeneratorFacade.implementation.injectDuplicates(this, duplicateProbability);
  }

  default RandomGenerator<T> ignoreExceptions(
      int maxThrows, Class<? extends Throwable>[] exceptionTypes) {
    return RandomGeneratorFacade.implementation.ignoreExceptions(this, exceptionTypes, maxThrows);
  }

  default RandomGenerator<T> dontShrink() {
    return random -> {
      Shrinkable<T> shrinkable = RandomGenerator.this.next(random).makeUnshrinkable();
      return shrinkable.makeUnshrinkable();
    };
  }
}
