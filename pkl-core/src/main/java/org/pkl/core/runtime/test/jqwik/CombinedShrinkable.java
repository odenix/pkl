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

public class CombinedShrinkable<T> implements Shrinkable<T> {
  private final List<Shrinkable<Object>> parts;
  private final Function<List<Object>, T> combinator;

  public CombinedShrinkable(List<Shrinkable<Object>> parts, Function<List<Object>, T> combinator) {
    this.parts = parts;
    this.combinator = combinator;
  }

  @Override
  public T value() {
    return createValue(parts);
  }

  private T createValue(List<Shrinkable<Object>> shrinkables) {
    return combinator.apply(createValues(shrinkables));
  }

  private List<Object> createValues(List<Shrinkable<Object>> shrinkables) {
    // Using loop instead of stream to make stack traces more readable
    List<Object> values = new ArrayList<>();
    for (Shrinkable<Object> shrinkable : shrinkables) {
      values.add(shrinkable.value());
    }
    return values;
  }

  @Override
  public Stream<Shrinkable<T>> shrink() {
    return shrinkPartsOneAfterTheOther();
  }

  protected Stream<Shrinkable<T>> shrinkPartsOneAfterTheOther() {
    List<Stream<Shrinkable<T>>> shrinkPerPartStreams = new ArrayList<>();
    for (int i = 0; i < parts.size(); i++) {
      int index = i;
      Shrinkable<Object> part = parts.get(i);
      Stream<Shrinkable<T>> shrinkElement =
          part.shrink()
              .flatMap(
                  shrunkElement -> {
                    List<Shrinkable<Object>> partsCopy = new ArrayList<>(parts);
                    partsCopy.set(index, shrunkElement);
                    return Stream.of(new CombinedShrinkable<>(partsCopy, combinator));
                  });
      shrinkPerPartStreams.add(shrinkElement);
    }
    return JqwikStreamSupport.concat(shrinkPerPartStreams);
  }

  @Override
  public ShrinkingDistance distance() {
    return ShrinkingDistance.combine(parts);
  }
}
