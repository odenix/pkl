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

/** Fluent interface to configure the generation of Character and char values. */
public interface CharacterArbitrary extends Arbitrary<Character> {

  /**
   * Allow all unicode chars to show up in generated values.
   *
   * <p>Resets previous settings.
   *
   * @return new instance of arbitrary
   */
  CharacterArbitrary all();

  /**
   * Allow all chars in {@code allowedChars} show up in generated values.
   *
   * <p>Adds to all already allowed chars.
   *
   * @param allowedChars chars allowed
   * @return new instance of arbitrary
   */
  CharacterArbitrary with(char... allowedChars);

  /**
   * Allow all chars in {@code allowedChars} show up in generated values.
   *
   * <p>Adds to all already allowed chars.
   *
   * @param allowedChars as String or other CharSequence
   * @return new instance of arbitrary
   */
  CharacterArbitrary with(CharSequence allowedChars);

  /**
   * Allow all chars within {@code min} (included) and {@code max} (included) to show up in
   * generated values.
   *
   * <p>Adds to all already allowed chars.
   *
   * @param min min char value
   * @param max max char value
   * @return new instance of arbitrary
   */
  CharacterArbitrary range(char min, char max);

  /**
   * Allow all ascii chars to show up in generated values.
   *
   * <p>Adds to all already allowed chars.
   *
   * @return new instance of arbitrary
   */
  CharacterArbitrary ascii();

  /**
   * Allow all numeric chars to show up in generated values.
   *
   * <p>Adds to all already allowed chars.
   *
   * @return new instance of arbitrary
   */
  CharacterArbitrary numeric();

  /**
   * Allow all whitespace chars to show up in generated values.
   *
   * <p>Adds to all already allowed chars.
   *
   * @return new instance of arbitrary
   */
  CharacterArbitrary whitespace();

  /**
   * Allow all alpha chars to show up in generated strings.
   *
   * <p>Adds to all already allowed chars.
   *
   * @return new instance of arbitrary
   */
  CharacterArbitrary alpha();
}
