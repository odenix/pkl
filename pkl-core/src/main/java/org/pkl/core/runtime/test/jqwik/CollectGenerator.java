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

public class CollectGenerator<T> implements RandomGenerator<List<T>> {
  private final RandomGenerator<T> elementGenerator;
  private final Predicate<List<T>> until;

  public CollectGenerator(RandomGenerator<T> elementGenerator, Predicate<List<T>> until) {
    this.elementGenerator = elementGenerator;
    this.until = until;
  }

  @Override
  public Shrinkable<List<T>> next(Random random) {
    List<T> base = new ArrayList<>();
    List<Shrinkable<T>> shrinkables = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      if (until.test(base)) {
        return new CollectShrinkable<>(shrinkables, until);
      }
      Shrinkable<T> shrinkable = elementGenerator.next(random);
      base.add(shrinkable.value());
      shrinkables.add(shrinkable);
    }
    String message =
        String.format("Generated list not fulfilled condition after maximum of %s elements", 10000);
    throw new JqwikException(message);
  }
}
