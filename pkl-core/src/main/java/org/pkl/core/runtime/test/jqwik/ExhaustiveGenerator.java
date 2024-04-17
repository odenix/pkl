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

import java.util.function.*;

/** Used only internally to run and compute exhaustive generation of parameters */
public interface ExhaustiveGenerator<T> extends Iterable<T> {

  long MAXIMUM_SAMPLES_TO_GENERATE = Integer.MAX_VALUE;

  class ExhaustiveGeneratorFacade {
    private static final ExhaustiveGeneratorFacade implementation;

    static {
      implementation = new ExhaustiveGeneratorFacade();
    }

    public <T, U> ExhaustiveGenerator<U> map(ExhaustiveGenerator<T> self, Function<T, U> mapper) {
      return new MappedExhaustiveGenerator<>(self, mapper);
    }

    public <T> ExhaustiveGenerator<T> filter(
        ExhaustiveGenerator<T> self, Predicate<T> filterPredicate, int maxMisses) {
      return new FilteredExhaustiveGenerator<>(self, filterPredicate, maxMisses);
    }

    public <T> ExhaustiveGenerator<T> injectNull(ExhaustiveGenerator<T> self) {
      return new WithNullExhaustiveGenerator<>(self);
    }

    public <T> ExhaustiveGenerator<T> ignoreExceptions(
        final ExhaustiveGenerator<T> self,
        final Class<? extends Throwable>[] exceptionTypes,
        int maxThrows) {
      return new IgnoreExceptionExhaustiveGenerator<>(self, exceptionTypes, maxThrows);
    }
  }

  /**
   * @return the maximum number of values that will be generated
   */
  long maxCount();

  default <U> ExhaustiveGenerator<U> map(Function<T, U> mapper) {
    return ExhaustiveGeneratorFacade.implementation.map(this, mapper);
  }

  default ExhaustiveGenerator<T> filter(Predicate<T> filterPredicate, int maxMisses) {
    return ExhaustiveGeneratorFacade.implementation.filter(this, filterPredicate, maxMisses);
  }

  default ExhaustiveGenerator<T> injectNull() {
    return ExhaustiveGeneratorFacade.implementation.injectNull(this);
  }

  default ExhaustiveGenerator<T> ignoreExceptions(
      int maxThrows, Class<? extends Throwable>[] exceptionTypes) {
    return ExhaustiveGeneratorFacade.implementation.ignoreExceptions(
        this, exceptionTypes, maxThrows);
  }
}
