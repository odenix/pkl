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

public class CollectShrinkable<T> implements Shrinkable<List<T>> {
  private final List<Shrinkable<T>> elements;
  private final Predicate<List<T>> until;

  public CollectShrinkable(List<Shrinkable<T>> elements, Predicate<List<T>> until) {
    this.elements = elements;
    this.until = until;
  }

  @Override
  public List<T> value() {
    return createValue(elements);
  }

  private List<T> createValue(List<Shrinkable<T>> elements) {
    return elements.stream().map(Shrinkable::value).collect(Collectors.toList());
  }

  @Override
  public Stream<Shrinkable<List<T>>> shrink() {
    return JqwikStreamSupport.concat(shrinkElementsOneAfterTheOther(), sortElements())
        .filter(s -> until.test(s.value()));
  }

  private Stream<Shrinkable<List<T>>> shrinkElementsOneAfterTheOther() {
    List<Stream<Shrinkable<List<T>>>> shrinkPerPartStreams = new ArrayList<>();
    for (int i = 0; i < elements.size(); i++) {
      int index = i;
      Shrinkable<T> part = elements.get(i);
      Stream<Shrinkable<List<T>>> shrinkElement =
          part.shrink()
              .flatMap(
                  shrunkElement -> {
                    Optional<List<Shrinkable<T>>> shrunkCollection =
                        collectElements(index, shrunkElement);
                    return shrunkCollection
                        .map(shrunkElements -> Stream.of(createShrinkable(shrunkElements)))
                        .orElse(Stream.empty());
                  });
      shrinkPerPartStreams.add(shrinkElement);
    }
    return JqwikStreamSupport.concat(shrinkPerPartStreams);
  }

  private Stream<Shrinkable<List<T>>> sortElements() {
    return ShrinkingCommons.sortElements(elements, this::createShrinkable);
  }

  private CollectShrinkable<T> createShrinkable(List<Shrinkable<T>> pairSwap) {
    return new CollectShrinkable<>(pairSwap, until);
  }

  private Optional<List<Shrinkable<T>>> collectElements(
      int replaceIndex, Shrinkable<T> shrunkElement) {
    List<Shrinkable<T>> newElements = new ArrayList<>();
    for (int i = 0; i < elements.size(); i++) {
      if (i == replaceIndex) {
        newElements.add(shrunkElement);
      } else {
        newElements.add(elements.get(i));
      }
      if (until.test(createValue(newElements))) {
        return Optional.of(newElements);
      }
    }
    return Optional.empty();
  }

  @Override
  public ShrinkingDistance distance() {
    return ShrinkingDistance.forCollection(elements);
  }
}
