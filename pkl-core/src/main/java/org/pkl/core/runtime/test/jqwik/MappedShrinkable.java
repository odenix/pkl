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

public class MappedShrinkable<T, U> implements Shrinkable<U> {

  private final Shrinkable<T> toMap;
  private final Function<T, U> mapper;

  public MappedShrinkable(Shrinkable<T> toMap, Function<T, U> mapper) {
    this.toMap = toMap;
    this.mapper = mapper;
  }

  @Override
  public U value() {
    return mapper.apply(toMap.value());
  }

  @Override
  public Stream<Shrinkable<U>> shrink() {
    return toMap.shrink().map(this::toMappedShrinkable);
  }

  public Shrinkable<U> toMappedShrinkable(Shrinkable<T> shrinkable) {
    return shrinkable.map(mapper);
  }

  @Override
  public Optional<Shrinkable<U>> grow(Shrinkable<?> before, Shrinkable<?> after) {
    if (before instanceof MappedShrinkable && after instanceof MappedShrinkable) {
      Shrinkable<?> beforeToMap = ((MappedShrinkable<?, ?>) before).toMap;
      Shrinkable<?> afterToMap = ((MappedShrinkable<?, ?>) after).toMap;
      return toMap.grow(beforeToMap, afterToMap).map(this::toMappedShrinkable);
    }
    return toMap.grow(before, after).map(this::toMappedShrinkable);
  }

  @Override
  public Stream<Shrinkable<U>> grow() {
    return toMap.grow().map(this::toMappedShrinkable);
  }

  @Override
  public ShrinkingDistance distance() {
    return toMap.distance();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MappedShrinkable<?, ?> that = (MappedShrinkable<?, ?>) o;
    return toMap.equals(that.toMap);
  }

  @Override
  public int hashCode() {
    return toMap.hashCode();
  }

  @Override
  public String toString() {
    return String.format("Mapped<%s>(%s)|%s", value().getClass().getSimpleName(), value(), toMap);
  }
}
