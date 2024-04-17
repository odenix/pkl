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

import java.util.List;

public class ChooseValueArbitrary<T> extends UseGeneratorsArbitrary<T> {

  private final int hashCode;
  private final List<T> values;

  public ChooseValueArbitrary(List<T> values) {
    super(
        RandomGenerators.choose(values),
        max -> ExhaustiveGenerators.choose(values, max),
        maxEdgeCases -> EdgeCasesSupport.choose(values, maxEdgeCases));
    hashCode = values.hashCode();
    this.values = values;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ChooseValueArbitrary<?> that = (ChooseValueArbitrary<?>) o;
    return values.equals(that.values);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}
