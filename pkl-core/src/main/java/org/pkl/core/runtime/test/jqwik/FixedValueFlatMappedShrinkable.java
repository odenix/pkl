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

import java.util.function.*;

public class FixedValueFlatMappedShrinkable<T, U> extends FlatMappedShrinkable<T, U> {

  private final Supplier<Shrinkable<U>> shrinkableSupplier;

  public FixedValueFlatMappedShrinkable(
      Shrinkable<T> toMap,
      Function<T, Shrinkable<U>> mapper,
      Supplier<Shrinkable<U>> shrinkableSupplier) {
    super(toMap, mapper);
    this.shrinkableSupplier = shrinkableSupplier;
  }

  @Override
  protected Shrinkable<U> shrinkable() {
    return shrinkableSupplier.get();
  }
}
