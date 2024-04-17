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
import java.util.concurrent.atomic.*;

public class ConcatIterator<T> implements Iterator<T> {

  private final List<Iterator<T>> iterators;
  private final AtomicInteger position;
  private Iterator<T> next;

  public ConcatIterator(List<Iterator<T>> iterators) {
    this.iterators = iterators;
    position = new AtomicInteger(0);
    if (!iterators.isEmpty()) {
      next = findNext();
    }
  }

  private Iterator<T> findNext() {
    while (!iterators.get(position.get()).hasNext()) {
      if (position.get() >= iterators.size() - 1) return null;
      position.getAndIncrement();
    }
    return iterators.get(position.get());
  }

  @Override
  public boolean hasNext() {
    return next != null && next.hasNext();
  }

  @Override
  public T next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    T current = next.next();
    next = findNext();
    return current;
  }
}
