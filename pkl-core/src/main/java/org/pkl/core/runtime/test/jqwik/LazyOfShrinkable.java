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

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class LazyOfShrinkable<T> implements Shrinkable<T> {
  public final Shrinkable<T> current;
  public final int depth;
  public final Set<LazyOfShrinkable<T>> parts;
  private final Function<LazyOfShrinkable<T>, Stream<Shrinkable<T>>> shrinker;

  public LazyOfShrinkable(
      Shrinkable<T> current,
      int depth,
      Set<LazyOfShrinkable<T>> parts,
      Function<LazyOfShrinkable<T>, Stream<Shrinkable<T>>> shrinker) {
    this.current = current;
    this.depth = depth;
    this.parts = parts;
    this.shrinker = shrinker;
  }

  @Override
  public T value() {
    return current.value();
  }

  @Override
  public Stream<Shrinkable<T>> shrink() {
    return shrinker.apply(this);
  }

  @Override
  public ShrinkingDistance distance() {
    return ShrinkingDistance.of(depth).append(current.distance());
  }
}
