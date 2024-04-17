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

public class WithNullExhaustiveGenerator<T> implements ExhaustiveGenerator<T> {
  private final ExhaustiveGenerator<T> base;

  public WithNullExhaustiveGenerator(ExhaustiveGenerator<T> base) {
    this.base = base;
  }

  @Override
  public long maxCount() {
    return base.maxCount() + 1;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      boolean nullDelivered = false;
      Iterator<T> iterator = base.iterator();

      @Override
      public boolean hasNext() {
        if (!nullDelivered) {
          return true;
        }
        return iterator.hasNext();
      }

      @Override
      public T next() {
        if (!nullDelivered) {
          nullDelivered = true;
          return null;
        }
        return iterator.next();
      }
    };
  }
}
