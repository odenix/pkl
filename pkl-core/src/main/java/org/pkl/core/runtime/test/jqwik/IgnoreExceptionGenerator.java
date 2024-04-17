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

import static org.pkl.core.runtime.test.jqwik.JqwikExceptionSupport.isInstanceOfAny;

import java.util.*;
import java.util.function.*;

public class IgnoreExceptionGenerator<T> implements RandomGenerator<T> {

  private final RandomGenerator<T> base;
  private final Class<? extends Throwable>[] exceptionTypes;
  private final int maxThrows;

  public IgnoreExceptionGenerator(
      RandomGenerator<T> base, Class<? extends Throwable>[] exceptionTypes, int maxThrows) {
    this.base = base;
    this.exceptionTypes = exceptionTypes;
    this.maxThrows = maxThrows;
  }

  @Override
  public Shrinkable<T> next(final Random random) {
    return new IgnoreExceptionShrinkable<>(nextUntilAccepted(random, base::next), exceptionTypes);
  }

  private Shrinkable<T> nextUntilAccepted(
      Random random, Function<Random, Shrinkable<T>> fetchShrinkable) {
    for (int i = 0; i < maxThrows; i++) {
      try {
        Shrinkable<T> next = fetchShrinkable.apply(random);
        // Enforce value generation for possible exception raising
        next.value();
        return next;
      } catch (Throwable throwable) {
        if (isInstanceOfAny(throwable, exceptionTypes)) {
          continue;
        }
        throw throwable;
      }
    }
    String message = String.format("%s missed more than %s times.", this, maxThrows);
    throw new JqwikException(message);
  }
}
