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

public class GenericEdgeCasesConfiguration<T> implements EdgeCases.Config<T> {
  private boolean none;
  private final List<Predicate<T>> filters = new ArrayList<>();
  private final List<T> additionalEdgeCases = new ArrayList<>();

  @Override
  public EdgeCases.Config<T> none() {
    none = true;
    return this;
  }

  @Override
  public EdgeCases.Config<T> filter(Predicate<T> filter) {
    filters.add(filter);
    return this;
  }

  @SafeVarargs
  @Override
  public final EdgeCases.Config<T> add(T... edgeCases) {
    for (T edgeCase : edgeCases) {
      checkEdgeCaseIsValid(edgeCase);
      additionalEdgeCases.add(edgeCase);
    }
    return this;
  }

  // Override in subclasses if there is anything to check
  protected void checkEdgeCaseIsValid(T edgeCase) {}

  @SafeVarargs
  @Override
  public final EdgeCases.Config<T> includeOnly(T... includedValues) {
    List<T> values = Arrays.asList(includedValues);
    return filter(values::contains);
  }

  public EdgeCases<T> configure(
      Consumer<EdgeCases.Config<T>> configurator,
      Function<Integer, EdgeCases<T>> edgeCasesCreator,
      int maxEdgeCases) {
    configurator.accept(this);

    EdgeCases<T> configuredEdgeCases;
    if (none) {
      configuredEdgeCases = EdgeCases.none();
    } else if (filters.isEmpty()) {
      configuredEdgeCases = edgeCasesCreator.apply(maxEdgeCases);
    } else {
      configuredEdgeCases = edgeCasesCreator.apply(Integer.MAX_VALUE);
    }

    List<Supplier<Shrinkable<T>>> suppliers = configuredEdgeCases.suppliers();
    for (Predicate<T> filter : new ArrayList<>(filters)) {
      suppliers =
          suppliers.stream().filter(s -> filter.test(s.get().value())).collect(Collectors.toList());
    }
    for (T additionalEdgeCase : additionalEdgeCases) {
      suppliers.add(() -> createShrinkable(additionalEdgeCase));
    }
    return EdgeCasesSupport.fromSuppliers(suppliers);
  }

  protected Shrinkable<T> createShrinkable(T additionalEdgeCase) {
    return Shrinkable.unshrinkable(additionalEdgeCase);
  }
}
