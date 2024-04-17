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

class MappedEdgeCasesConsumer<T, U> implements Consumer<EdgeCases.Config<U>> {

  private final Consumer<EdgeCases.Config<T>> tConfigurator;
  private final Function<U, T> utMapper;
  private final Function<T, U> tuMapper;

  MappedEdgeCasesConsumer(
      Consumer<EdgeCases.Config<T>> tConfigurator,
      Function<U, T> utMapper,
      Function<T, U> tuMapper) {
    this.tConfigurator = tConfigurator;
    this.utMapper = utMapper;
    this.tuMapper = tuMapper;
  }

  @Override
  public void accept(EdgeCases.Config<U> uConfig) {

    EdgeCases.Config<T> tConfig =
        new EdgeCases.Config<T>() {
          @Override
          public EdgeCases.Config<T> none() {
            uConfig.none();
            return this;
          }

          @Override
          public EdgeCases.Config<T> filter(Predicate<T> filter) {
            uConfig.filter(u -> filter.test(utMapper.apply(u)));
            return this;
          }

          @SuppressWarnings("unchecked")
          @SafeVarargs
          @Override
          public final EdgeCases.Config<T> add(T... edgeCases) {
            for (T edgeCase : edgeCases) {
              uConfig.add(tuMapper.apply(edgeCase));
            }
            return this;
          }

          @SuppressWarnings("unchecked")
          @Override
          public EdgeCases.Config<T> includeOnly(T... includedValues) {
            Object[] includedBigIntegers = new Object[includedValues.length];
            for (int i = 0; i < includedValues.length; i++) {
              includedBigIntegers[i] = tuMapper.apply(includedValues[i]);
            }
            uConfig.includeOnly((U[]) includedBigIntegers);
            return this;
          }
        };
    tConfigurator.accept(tConfig);
  }
}
