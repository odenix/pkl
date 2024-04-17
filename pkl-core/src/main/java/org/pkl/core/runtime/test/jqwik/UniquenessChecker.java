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
import java.util.stream.*;

public class UniquenessChecker {

  public static <T> boolean checkShrinkableUniqueIn(
      Collection<FeatureExtractor<T>> extractors,
      Shrinkable<T> shrinkable,
      List<Shrinkable<T>> shrinkables) {
    if (extractors.isEmpty()) {
      return true;
    }
    T value = shrinkable.value();
    List<T> elements = shrinkables.stream().map(Shrinkable::value).collect(Collectors.toList());
    return checkValueUniqueIn(extractors, value, elements);
  }

  public static <T> boolean checkValueUniqueIn(
      Collection<FeatureExtractor<T>> extractors, T value, Collection<T> elements) {
    for (FeatureExtractor<T> extractor : extractors) {
      if (!extractor.isUniqueIn(value, elements)) {
        return false;
      }
    }
    return true;
  }

  public static <T> boolean checkUniquenessOfShrinkables(
      Collection<FeatureExtractor<T>> extractors, List<Shrinkable<T>> shrinkables) {
    if (extractors.isEmpty()) {
      return true;
    }
    List<T> elements = shrinkables.stream().map(Shrinkable::value).collect(Collectors.toList());
    return checkUniquenessOfValues(extractors, elements);
  }

  public static <T> boolean checkUniquenessOfValues(
      Collection<FeatureExtractor<T>> extractors, Collection<T> elements) {
    for (FeatureExtractor<T> extractor : extractors) {
      if (!extractor.areUnique(elements)) {
        return false;
      }
    }
    return true;
  }
}
