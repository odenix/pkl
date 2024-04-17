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

import java.math.*;
import java.util.*;

/**
 * Determines how generated numerical values are generated and distributed across the allowed range
 * and a center withing this range.
 *
 * <p>Since all random numeric value generation is going back to {@linkplain BigDecimal} generation
 * this interfaces uses only values of type {@linkplain BigDecimal}.
 *
 * <p>The generation of an arbitrary's edge cases is not influenced by the distribution.
 */
public interface RandomDistribution {

  class RandomDistributionFacade {
    private static final RandomDistribution.RandomDistributionFacade implementation;
    private static final BiasedRandomDistribution BIASED_RANDOM_DISTRIBUTION =
        new BiasedRandomDistribution();
    private static final UniformRandomDistribution UNIFORM_RANDOM_DISTRIBUTION =
        new UniformRandomDistribution();

    static {
      implementation = new RandomDistributionFacade();
    }

    public RandomDistribution biased() {
      return BIASED_RANDOM_DISTRIBUTION;
    }

    public RandomDistribution uniform() {
      return UNIFORM_RANDOM_DISTRIBUTION;
    }

    public RandomDistribution gaussian(double borderSigma) {
      return new GaussianRandomDistribution(borderSigma);
    }
  }

  /** Generator for BigInteger values which are behind all generated numeric values in jqwik. */
  interface RandomNumericGenerator {

    /**
     * Generate next random number within the specified range given on creation of the generator.
     *
     * @param random A random value to use for random generation
     * @return an instance of BigInteger. Never {@code null}.
     */
    BigInteger next(Random random);
  }

  /**
   * A distribution that generates values closer to the center of a numerical range with a higher
   * probability. The bigger the range the stronger the bias.
   *
   * @return a random distribution instance
   */
  static RandomDistribution biased() {
    return RandomDistributionFacade.implementation.biased();
  }

  /**
   * A distribution that generates values across the allowed range with a uniform probability
   * distribution.
   *
   * @return a random distribution instance
   */
  static RandomDistribution uniform() {
    return RandomDistributionFacade.implementation.uniform();
  }

  /**
   * A distribution that generates values with (potentially asymmetric) gaussian distribution the
   * mean of which is the specified center and the probability at the borders is approximately
   * {@code borderSigma} times standard deviation.
   *
   * <p>Gaussian generation is approximately 10 times slower than {@linkplain #biased()} or
   * {@linkplain #uniform()} generation. But still, except in rare cases this will not make a
   * noticeable difference in the runtime of your properties.
   *
   * @param borderSigma The approximate factor of standard deviation at the border(s)
   * @return a random distribution instance
   */
  static RandomDistribution gaussian(double borderSigma) {
    return RandomDistributionFacade.implementation.gaussian(borderSigma);
  }

  /**
   * A gaussian distribution with {@code borderSigma} of 3, i.e. approximately 99.7% of values are
   * within the borders.
   *
   * @see #gaussian(double)
   * @return a random distribution instance
   */
  static RandomDistribution gaussian() {
    return RandomDistributionFacade.implementation.gaussian(3);
  }

  /**
   * Return a generator that will generate value with the desired distribution
   *
   * @param genSize The approximate number of values to generate. Can be influenced by callers.
   * @param min The minimum allowed value (included)
   * @param max The maximum allowed value (included)
   * @param center The center for the distribution. Must be within min and max.
   * @return generator for randomly generated BigInteger values
   */
  RandomNumericGenerator createGenerator(
      int genSize, BigInteger min, BigInteger max, BigInteger center);
}
