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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseCharacterArbitrary extends UseGeneratorsArbitrary<Character> {

  private final char[] chars;

  public ChooseCharacterArbitrary(char[] chars) {
    super(
        RandomGenerators.choose(chars),
        max -> ExhaustiveGenerators.choose(chars, max),
        maxEdgeCases -> {
          List<Character> validCharacters = new ArrayList<>(chars.length);
          for (char character : chars) {
            validCharacters.add(character);
          }
          return EdgeCasesSupport.choose(validCharacters, maxEdgeCases);
        });
    this.chars = chars;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ChooseCharacterArbitrary that = (ChooseCharacterArbitrary) o;
    return Arrays.equals(chars, that.chars);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(chars);
  }
}
