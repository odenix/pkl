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

public class Unshrinkable<T> implements Shrinkable<T> {
  private final ShrinkingDistance distance;
  private final Supplier<T> valueSupplier;

  public Unshrinkable(Supplier<T> valueSupplier, ShrinkingDistance distance) {
    this.valueSupplier = valueSupplier;
    this.distance = distance;
  }

  @Override
  public T value() {
    return valueSupplier.get();
  }

  @Override
  public Stream<Shrinkable<T>> shrink() {
    return Stream.empty();
  }

  @Override
  public ShrinkingDistance distance() {
    return distance;
  }

  @Override
  public String toString() {
    return JqwikStringSupport.displayString(value());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Unshrinkable<?> that = (Unshrinkable<?>) o;

    return Objects.equals(value(), that.value());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value());
  }
}
