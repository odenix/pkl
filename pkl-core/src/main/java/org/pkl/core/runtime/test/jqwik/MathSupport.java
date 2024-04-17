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

public class MathSupport {

  public static long factorial(long number) {
    if (number > 20) {
      throw new ArithmeticException("MathSupport.factorial() only works till 20");
    }
    long result = 1;

    for (long factor = 2; factor <= number; factor++) {
      result *= factor;
    }

    return result;
  }

  // From https://rosettacode.org/wiki/Evaluate_binomial_coefficients#Java
  // Faster than return factorial(n) / (factorial(n - k) * factorial(k));
  // Max n = 70
  public static long binomial(int n, int k) {
    if (n > 70) {
      throw new ArithmeticException("MathSupport.binomial() only works till 70");
    }
    if (k > n - k) {
      k = n - k;
    }

    long b = 1;
    for (int i = 1, m = n; i <= k; i++, m--) {
      b = b * m / i;
    }
    return b;
  }
}
