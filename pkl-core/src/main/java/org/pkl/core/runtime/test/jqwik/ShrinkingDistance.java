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

import static org.pkl.core.runtime.test.jqwik.ShrinkingDistanceArraysSupport.at;
import static org.pkl.core.runtime.test.jqwik.ShrinkingDistanceArraysSupport.concatenate;
import static org.pkl.core.runtime.test.jqwik.ShrinkingDistanceArraysSupport.sumUp;

import java.util.*;
import java.util.stream.*;
import org.pkl.core.util.Nullable;

/**
 * A {@code ShrinkingDistance} is a measure of how close a value is to the minimum value, aka target
 * value.
 *
 * <p>The distance is used during shrinking to determine if a shrunk value is really closer to the
 * target value. If it is not, the value is being discarded.
 */
public class ShrinkingDistance implements Comparable<ShrinkingDistance> {

  public static final ShrinkingDistance MAX = ShrinkingDistance.of(Long.MAX_VALUE);

  public static final ShrinkingDistance MIN = ShrinkingDistance.of(0);

  private final long[] distances;

  /**
   * Create a {@code ShrinkingDistance} with one or more dimensions.
   *
   * @param distances a non-empty array of non-negative values.
   * @return an immutable instance of {@code ShrinkingDistance}
   */
  public static ShrinkingDistance of(long... distances) {
    if (distances.length == 0) {
      throw new IllegalArgumentException("ShrinkingDistance requires at least one value");
    }
    if (Arrays.stream(distances).anyMatch(d -> d < 0)) {
      throw new IllegalArgumentException("ShrinkingDistance does not allow negative values");
    }
    return new ShrinkingDistance(distances);
  }

  public static <T> ShrinkingDistance forCollection(Collection<Shrinkable<T>> elements) {
    // This is an optimization to avoid creating temporary arrays, which the old streams-based
    // implementation did.
    long[] collectedDistances = sumUp(toDistances(elements));
    ShrinkingDistance sumDistanceOfElements = new ShrinkingDistance(collectedDistances);
    return ShrinkingDistance.of(elements.size()).append(sumDistanceOfElements);
  }

  public static <T> ShrinkingDistance combine(List<Shrinkable<T>> shrinkables) {
    // This can happen e.g. when using Combinators.combine() with an empty list of arbitraries.
    if (shrinkables.isEmpty()) {
      return ShrinkingDistance.MIN;
    }

    // This is an optimization to avoid creating temporary arrays, which the old streams-based
    // implementation did.
    long[] combinedDistances = concatenate(toDistances(shrinkables));
    return new ShrinkingDistance(combinedDistances);
  }

  private ShrinkingDistance(long[] distances) {
    this.distances = distances;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ShrinkingDistance that = (ShrinkingDistance) o;
    return Arrays.equals(this.distances, that.distances);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(distances);
  }

  @Override
  public String toString() {
    return String.format("ShrinkingDistance:%s", Arrays.toString(distances));
  }

  /**
   * Compare to distances with each other. No distance can be greater than {@link #MAX}. No distance
   * can be smaller than {@link #MIN}.
   */
  @Override
  public int compareTo(ShrinkingDistance other) {
    if (this == MAX) {
      if (other == MAX) {
        return 0;
      } else {
        return 1;
      }
    }
    if (other == MAX) {
      return -1;
    }
    int dimensionsToCompare = Math.max(size(), other.size());
    for (int i = 0; i < dimensionsToCompare; i++) {
      int compareDimensionResult = compareDimension(other, i);
      if (compareDimensionResult != 0) return compareDimensionResult;
    }
    return 0;
  }

  public List<ShrinkingDistance> dimensions() {
    return Arrays.stream(distances).mapToObj(ShrinkingDistance::of).collect(Collectors.toList());
  }

  public int size() {
    return distances.length;
  }

  private int compareDimension(ShrinkingDistance other, int i) {
    long left = at(distances, i);
    long right = at(other.distances, i);
    return Long.compare(left, right);
  }

  public ShrinkingDistance plus(ShrinkingDistance other) {
    long[] summedUpDistances = sumUp(Arrays.asList(distances, other.distances));
    return new ShrinkingDistance(summedUpDistances);
  }

  public ShrinkingDistance append(ShrinkingDistance other) {
    long[] appendedDistances = concatenate(Arrays.asList(distances, other.distances));
    return new ShrinkingDistance(appendedDistances);
  }

  private static <T> List<long[]> toDistances(Collection<Shrinkable<T>> shrinkables) {
    List<long[]> listOfDistances = new ArrayList<>(shrinkables.size());
    for (Shrinkable<?> tShrinkable : shrinkables) {
      long[] longs = tShrinkable.distance().distances;
      listOfDistances.add(longs);
    }
    return listOfDistances;
  }
}
