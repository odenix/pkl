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

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.pkl.core.runtime.test.jqwik.Tuple.Tuple3;

public class Memoize {

  private static Store<Map<Tuple3<Arbitrary<?>, Integer, Boolean>, RandomGenerator<?>>>
      generatorStore() {
    return Store.getOrCreate(Memoize.class, Lifespan.PROPERTY, () -> new LruCache<>(500));
  }

  @SuppressWarnings("unchecked")
  public static <U> RandomGenerator<U> memoizedGenerator(
      Arbitrary<? extends U> arbitrary,
      int genSize,
      boolean withEdgeCases,
      Supplier<RandomGenerator<? extends U>> generatorSupplier) {
    if (!arbitrary.isGeneratorMemoizable()) {
      return (RandomGenerator<U>) generatorSupplier.get();
    }

    Tuple3<Arbitrary<?>, Integer, Boolean> key = Tuple.of(arbitrary, genSize, withEdgeCases);
    RandomGenerator<?> generator =
        computeIfAbsent(generatorStore().get(), key, ignore -> generatorSupplier.get());
    return (RandomGenerator<U>) generator;
  }

  // Had to roll my on computeIfAbsent because HashMap.computeIfAbsent()
  // does not allow modifications of the map within the mapping function
  private static <K, V> V computeIfAbsent(
      Map<K, V> cache, K key, Function<? super K, ? extends V> mappingFunction) {
    V result = cache.get(key);

    if (result == null) {
      result = mappingFunction.apply(key);
      cache.put(key, result);
    }

    return result;
  }
}
