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

class ShrinkingDistanceArraysSupport {

  static long at(long[] array, int i) {
    return array.length > i ? array[i] : 0;
  }

  static long[] sumUp(List<long[]> listOfArrays) {
    long[] summedUpArray = new long[maxLength(listOfArrays)];
    for (long[] array : listOfArrays) {
      for (int i = 0; i < summedUpArray.length; i++) {
        summedUpArray[i] = plusWithoutOverflowAt(summedUpArray, array, i);
      }
    }
    return summedUpArray;
  }

  private static int maxLength(List<long[]> listOfArrays) {
    int maxDistanceSize = 0;
    for (long[] array : listOfArrays) {
      maxDistanceSize = Math.max(maxDistanceSize, array.length);
    }
    return maxDistanceSize;
  }

  private static long plusWithoutOverflowAt(long[] left, long[] right, int index) {
    long summedValue = at(right, index) + at(left, index);
    if (summedValue < 0) {
      return Long.MAX_VALUE;
    }
    return summedValue;
  }

  static long[] concatenate(List<long[]> listOfArrays) {
    int size = listOfArrays.stream().mapToInt(s -> s.length).sum();
    long[] concatenatedArrays = new long[size];
    int i = 0;
    for (long[] array : listOfArrays) {
      System.arraycopy(array, 0, concatenatedArrays, i, array.length);
      i += array.length;
    }
    return concatenatedArrays;
  }
}
