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

public class InjectDuplicatesGenerator<T> implements RandomGenerator<T> {

  private final RandomGenerator<T> base;
  private final double duplicateProbability;
  private final Store<List<Long>> previousSeedsStore;

  public InjectDuplicatesGenerator(RandomGenerator<T> base, double duplicateProbability) {
    this.base = base;
    this.duplicateProbability = duplicateProbability;
    this.previousSeedsStore = createPreviousSeedsStorePerTry();
  }

  private Store<List<Long>> createPreviousSeedsStorePerTry() {
    return Store.getOrCreate(this, Lifespan.TRY, ArrayList::new);
  }

  @Override
  public Shrinkable<T> next(Random random) {
    long seed = chooseSeed(random);
    return base.next(SourceOfRandomness.newRandom(seed));
  }

  long chooseSeed(Random random) {
    List<Long> previousSeeds = previousSeedsStore.get();
    if (!previousSeeds.isEmpty()) {
      if (random.nextDouble() <= duplicateProbability) {
        return randomPreviousSeed(previousSeedsStore, random);
      }
    }
    long seed = random.nextLong();
    previousSeeds.add(seed);
    return seed;
  }

  private long randomPreviousSeed(Store<List<Long>> previousSeeds, Random random) {
    int index = random.nextInt(previousSeeds.get().size());
    return previousSeeds.get().get(index);
  }
}
