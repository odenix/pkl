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

/** Fluent interface to configure arbitraries that generate String values. */
public interface StringArbitrary extends Arbitrary<String> {

  /**
   * Set the maximum allowed length {@code maxLength} (included) of generated strings.
   *
   * @throws IllegalArgumentException if maxLength &lt; 0 or maxLength &lt; min length
   */
  StringArbitrary ofMaxLength(int maxLength);

  /**
   * Set the minimum allowed length {@code minLength} (included) of generated strings. This will
   * also set the max length of the string if {@code minLength} is larger than the current max
   * length.
   *
   * @throws IllegalArgumentException if minLength &lt; 0
   */
  StringArbitrary ofMinLength(int minLength);

  /**
   * Fix the length to {@code length} of generated strings.
   *
   * @throws IllegalArgumentException if length &lt; 0
   */
  default StringArbitrary ofLength(int length) {
    return ofMinLength(length).ofMaxLength(length);
  }

  /**
   * Allow all chars in {@code chars} to show up in generated strings.
   *
   * <p>Can be combined with other methods that allow chars.
   */
  StringArbitrary withChars(char... chars);

  /**
   * Allow all chars in {@code chars} to show up in generated strings.
   *
   * <p>Can be combined with other methods that allow chars.
   */
  StringArbitrary withChars(CharSequence chars);

  /**
   * Allow all chars within {@code from} (included) and {@code to} (included) to show up in
   * generated strings.
   *
   * <p>Can be combined with other methods that allow chars.
   */
  StringArbitrary withCharRange(char from, char to);

  /**
   * Allow all ascii chars to show up in generated strings.
   *
   * <p>Can be combined with other methods that allow chars.
   */
  StringArbitrary ascii();

  /**
   * Allow all alpha chars to show up in generated strings.
   *
   * <p>Can be combined with other methods that allow chars.
   */
  StringArbitrary alpha();

  /**
   * Allow all numeric chars (digits) to show up in generated strings.
   *
   * <p>Can be combined with other methods that allow chars.
   */
  StringArbitrary numeric();

  /**
   * Allow all chars that will return {@code true} for {@link Character#isWhitespace(char)}.
   *
   * <p>Can be combined with other methods that allow chars.
   */
  StringArbitrary whitespace();

  /**
   * Allow all unicode chars even noncharacters and private use characters but only in plane 0 (aka
   * Basic Multilingual Plane)
   */
  StringArbitrary all();

  /**
   * Exclude all {@code charsToExclude} from generated strings
   *
   * @param charsToExclude chars to exclude
   * @return new instance of arbitrary
   */
  StringArbitrary excludeChars(char... charsToExclude);

  /**
   * Set random distribution {@code distribution} of length of generated string. The distribution's
   * center is the minimum length of the generated list.
   */
  StringArbitrary withLengthDistribution(RandomDistribution lengthDistribution);

  /**
   * Set the probability for repeating chars within the string to an approximate probability value.
   *
   * <p>Setting @code{repeatProbability} to 0.0 will generate strings with unique chars, i.e., it is
   * equivalent to calling {@link #uniqueChars()}.
   *
   * @param repeatProbability Must be between 0 (included) and 1 (excluded)
   */
  StringArbitrary repeatChars(double repeatProbability);

  /** Prevent character from having duplicates within the generated string. */
  StringArbitrary uniqueChars();
}
