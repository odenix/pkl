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
import java.util.stream.*;

public class ChooseValueShrinkable<T> extends AbstractValueShrinkable<T> {

  private final List<T> values;

  public ChooseValueShrinkable(T value, List<T> values) {
    super(value);
    this.values = values;
  }

  @Override
  public ShrinkingDistance distance() {
    return ShrinkingDistance.of(values.indexOf(value()));
  }

  @Override
  public Stream<Shrinkable<T>> shrink() {
    int index = values.indexOf(this.value());
    if (index == 0) {
      return Stream.empty();
    }
    return values.subList(0, index).stream()
        .map(value -> new ChooseValueShrinkable<>(value, values));
  }

  @Override
  public Stream<Shrinkable<T>> grow() {
    int index = values.indexOf(this.value());
    if (index == values.size() - 1) {
      return Stream.empty();
    }
    return values.subList(index + 1, values.size()).stream()
        .map(value -> new ChooseValueShrinkable<>(value, values));
  }
}
