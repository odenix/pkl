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

public class SampleShrinkable<T> extends AbstractValueShrinkable<T> {

  private final List<T> samples;
  private final int index;

  @SafeVarargs
  public static <T> List<Shrinkable<T>> listOf(T... samples) {
    List<T> samplesList = Arrays.asList(samples);
    List<Shrinkable<T>> shrinkables = new ArrayList<>();
    for (int i = 0; i < samples.length; i++) {
      shrinkables.add(new SampleShrinkable<>(samplesList, i));
    }
    return shrinkables;
  }

  private SampleShrinkable(List<T> samples, int index) {
    super(samples.get(index));
    this.samples = samples;
    this.index = index;
  }

  @Override
  public Stream<Shrinkable<T>> shrink() {
    int sampleIndex = this.index;
    if (sampleIndex == 0) return Stream.empty();
    return Stream.of(new SampleShrinkable<>(samples, sampleIndex - 1));
  }

  @Override
  public ShrinkingDistance distance() {
    return ShrinkingDistance.of(index);
  }
}
