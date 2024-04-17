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

public class FilteredExhaustiveGenerator<T> implements ExhaustiveGenerator<T> {
  private final ExhaustiveGenerator<T> toFilter;
  private final Predicate<T> filter;
  private int maxMisses;

  public FilteredExhaustiveGenerator(
      ExhaustiveGenerator<T> toFilter, Predicate<T> filter, int maxMisses) {
    this.toFilter = toFilter;
    this.filter = filter;
    this.maxMisses = maxMisses;
  }

  @Override
  public long maxCount() {
    return toFilter.maxCount();
  }

  @Override
  public Iterator<T> iterator() {
    final Iterator<T> mappedIterator = toFilter.iterator();
    return new Iterator<T>() {

      T next = findNext();

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public T next() {
        if (next == null) {
          throw new NoSuchElementException();
        }
        T result = next;
        next = findNext();
        return result;
      }

      private T findNext() {
        for (int i = 0; i < maxMisses; i++) {
          if (!mappedIterator.hasNext()) {
            return null;
          }
          T value = mappedIterator.next();
          if (filter.test(value)) {
            return value;
          }
        }
        String message = String.format("Filter missed more than %s times.", maxMisses);
        throw new JqwikException(message);
      }
    };
  }
}
