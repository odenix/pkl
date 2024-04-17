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

/** Fluent interface to configure the generation of Double and double values. */
public interface DoubleArbitrary extends NumericalArbitrary<Double, DoubleArbitrary> {

  /**
   * Set the allowed lower {@code min} (included) and upper {@code max} (included) border of
   * generated numbers.
   *
   * @param min The lower border of possible values
   * @param max The upper border of possible values
   * @return new instance of arbitrary
   */
  default DoubleArbitrary between(double min, double max) {
    return between(min, true, max, true);
  }

  /**
   * Set the allowed lower {@code min} (included) and upper {@code max} (included) border of
   * generated numbers. Specify if borders should be included in allowed values or not.
   *
   * @param min The lower border of possible values
   * @param minIncluded Should the lower border be included
   * @param max The upper border of possible values
   * @param maxIncluded Should the upper border be included
   * @return new instance of arbitrary
   */
  DoubleArbitrary between(double min, boolean minIncluded, double max, boolean maxIncluded);

  /**
   * Set the allowed lower {@code min} (included) border of generated numbers.
   *
   * @param min The lower border of possible values
   * @return new instance of arbitrary
   */
  DoubleArbitrary greaterOrEqual(double min);

  /**
   * Set the allowed lower {@code min} (excluded) border of generated numbers.
   *
   * @param min The lower border of possible values
   * @return new instance of arbitrary
   */
  DoubleArbitrary greaterThan(double min);

  /**
   * Set the allowed upper {@code max} (included) border of generated numbers.
   *
   * @param max The upper border of possible values
   * @return new instance of arbitrary
   */
  DoubleArbitrary lessOrEqual(double max);

  /**
   * Set the allowed upper {@code max} (excluded) border of generated numbers.
   *
   * @param max The upper border of possible values
   * @return new instance of arbitrary
   */
  DoubleArbitrary lessThan(double max);

  /**
   * Set the scale (maximum number of decimal places) to {@code scale}.
   *
   * @param scale number of decimal places
   * @return new instance of arbitrary
   */
  DoubleArbitrary ofScale(int scale);

  /**
   * Set shrinking target to {@code target} which must be between the allowed bounds.
   *
   * @param target The value which is considered to be the most simple value for shrinking
   * @return new instance of arbitrary
   */
  DoubleArbitrary shrinkTowards(double target);

  /**
   * Inject a special value into generated values and edge cases. This value can be outside the
   * constraints of the arbitrary, e.g. have more decimals than specified by {@linkplain
   * #ofScale(int)}.
   *
   * @param special value
   * @return new instance of arbitrary
   */
  DoubleArbitrary withSpecialValue(double special);

  /**
   * Inject a selection of special values using {@linkplain #withSpecialValue(double)}:
   *
   * <ul>
   *   <li>{@linkplain Double#NaN}
   *   <li>{@linkplain Double#MIN_VALUE}
   *   <li>{@linkplain Double#MIN_NORMAL}
   *   <li>{@linkplain Double#POSITIVE_INFINITY}
   *   <li>{@linkplain Double#NEGATIVE_INFINITY}
   * </ul>
   *
   * This value can be outside the constraints of the arbitrary, e.g. have more decimals than
   * specified by {@linkplain #ofScale(int)}.
   *
   * @return new instance of arbitrary
   */
  DoubleArbitrary withStandardSpecialValues();
}
