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

class IterableBasedExhaustiveGenerator<T> implements ExhaustiveGenerator<T> {

  private final Iterable<T> iterable;
  private final long maxCount;

  IterableBasedExhaustiveGenerator(Iterable<T> iterable, long maxCount) {
    this.iterable = iterable;
    this.maxCount = maxCount;
  }

  @Override
  public long maxCount() {
    return maxCount;
  }

  @Override
  public Iterator<T> iterator() {
    return iterable.iterator();
  }
}
