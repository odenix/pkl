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

import static java.lang.Math.*;

import java.util.*;
import java.util.stream.*;

public class EdgeCasesGenerator implements Iterator<List<Shrinkable<Object>>> {

  // Caveat: Always make sure that the number is greater than 1.
  // Otherwise only edge cases will be generated
  // Currently the value is always between 5 and 20
  public static int calculateBaseToEdgeCaseRatio(int genSize, int countEdgeCases) {
    return min(max(genSize / countEdgeCases, 5), 20);
  }

  private final List<EdgeCases<Object>> edgeCases;
  private final Iterator<List<Shrinkable<Object>>> iterator;

  EdgeCasesGenerator(List<EdgeCases<Object>> edgeCases) {
    this.edgeCases = edgeCases;
    this.iterator = createIterator();
  }

  private Iterator<List<Shrinkable<Object>>> createIterator() {
    if (this.edgeCases.isEmpty()) {
      return Collections.emptyIterator();
    }
    List<Iterable<Shrinkable<Object>>> iterables =
        edgeCases.stream()
            .map(edge -> (Iterable<Shrinkable<Object>>) edge)
            .collect(Collectors.toList());
    return Combinatorics.combine(iterables);
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public List<Shrinkable<Object>> next() {
    return iterator.next();
  }
}
