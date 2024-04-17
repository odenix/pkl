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

public interface EdgeCases<T> extends Iterable<Shrinkable<T>> {

  class EdgeCasesFacade {
    private static final EdgeCases.EdgeCasesFacade implementation;

    static {
      implementation = new EdgeCasesFacade();
    }

    public <T> EdgeCases<T> fromSuppliers(final List<Supplier<Shrinkable<T>>> suppliers) {
      return EdgeCasesSupport.fromSuppliers(suppliers);
    }
  }

  interface Config<T> {

    static <T> Consumer<Config<T>> noConfig() {
      return config -> {};
    }

    /**
     * Don't use any of the default edge cases
     *
     * @return same configuration instance
     */
    Config<T> none();

    /**
     * Only include default edge cases for which {@linkplain #filter(Predicate)} returns true
     *
     * @param filter A predicate
     * @return same configuration instance
     */
    Config<T> filter(Predicate<T> filter);

    /**
     * Add one or more unshrinkable additional values as edge cases. In general, edge cases you add
     * here must be values within the allowed value range of the current arbitrary. You add them as
     * edge cases to make sure they are generated with a very high probability.
     *
     * <p>Some arbitraries may allow added values to be outside the allowed value range. This is
     * mainly due to implementation issues and should not rely on it. Adding impossible values will
     * - sadly enough - not raise an exception nor log a warning.
     *
     * @param edgeCases The edge cases to add to default edge cases.
     * @return same configuration instance
     */
    @SuppressWarnings("unchecked")
    Config<T> add(T... edgeCases);

    /**
     * Include only the values given, and only if they are in the set of default edge cases.
     *
     * @param includedValues The values to be included
     * @return same configuration instance
     */
    @SuppressWarnings("unchecked")
    Config<T> includeOnly(T... includedValues);
  }

  List<Supplier<Shrinkable<T>>> suppliers();

  default int size() {
    return suppliers().size();
  }

  default boolean isEmpty() {
    return size() == 0;
  }

  default Iterator<Shrinkable<T>> iterator() {
    return suppliers().stream().map(Supplier::get).iterator();
  }

  static <T> EdgeCases<T> fromSuppliers(List<Supplier<Shrinkable<T>>> suppliers) {
    return EdgeCasesFacade.implementation.fromSuppliers(suppliers);
  }

  static <T> EdgeCases<T> none() {
    return fromSuppliers(Collections.emptyList());
  }

  static <T> EdgeCases<T> fromSupplier(Supplier<Shrinkable<T>> supplier) {
    return fromSuppliers(Collections.singletonList(supplier));
  }
}
