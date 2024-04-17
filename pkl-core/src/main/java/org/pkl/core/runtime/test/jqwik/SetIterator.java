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

public class SetIterator<T> implements Iterator<Set<T>> {

  private final Iterator<List<T>> combinedListIterator;
  private final Set<Set<T>> generatedSets = new LinkedHashSet<>();
  private final int setSize;
  private Set<T> next;

  public SetIterator(Iterable<T> elementIterable, int setSize) {
    this.setSize = setSize;
    List<Iterable<T>> iterables = new ArrayList<>();
    for (int i = 0; i < setSize; i++) {
      iterables.add(elementIterable);
    }
    combinedListIterator = new CombinedIterator<>(iterables);
    next = findNext();
  }

  private Set<T> findNext() {
    while (combinedListIterator.hasNext()) {
      HashSet<T> candidate = new LinkedHashSet<>(combinedListIterator.next());
      if (candidate.size() != setSize || generatedSets.contains(candidate)) {
        continue;
      }
      generatedSets.add(candidate);
      return candidate;
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public Set<T> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    Set<T> current = next;
    next = findNext();
    return current;
  }
}
