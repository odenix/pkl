/*
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
package org.pkl.core.util;

import java.util.function.IntFunction;

/**
 * A builder for typed arrays that is convenient, efficient, and safe for Truffle partial evaluation.
 */
public final class ArrayBuilder<T> {
  final static int INITIAL_LENGTH = 8;
  
  private final IntFunction<T[]> factory;
  private T @Nullable[] array;
  private int nextIndex;

  /**
   * Returns a new builder that creates arrays with the specified factory.
   * The first time an element is added, an array of length 8 is created.
   * Whenever the array becomes too small, it is grown to twice its previous length.
   */
  public static <T> ArrayBuilder<T> of(IntFunction<T[]> factory) {
    return new ArrayBuilder<>(factory);
  }

  private ArrayBuilder(IntFunction<T[]> factory) {
    this.factory = factory;
  }

  /**
   * Adds {@code value} to the array.
   */
  public void add(T value) {
    if (array == null) {
      array = factory.apply(INITIAL_LENGTH);
    } else if (nextIndex >= array.length) {
      var newArray = factory.apply(Math.multiplyExact(array.length, 2));
      System.arraycopy(array, 0, newArray, 0, array.length);
      array = newArray;
    }
    array[nextIndex] = value;
    nextIndex += 1;
  }

  /**
   * Returns the current array, which may be larger than the number of elements added.
   */
  @SuppressWarnings("unchecked")
  public <R extends T> R[] toOversizedArray() {
    return array == null ? (R[]) factory.apply(0) : (R[]) array;
  }

  /**
   * Returns an array that exactly fits the number of elements added, potentially requiring a copy.
   */
  @SuppressWarnings("unchecked")
  public <R extends T> R[] toArray() {
    if (array == null) {
      return (R[]) factory.apply(0);
    }
    if (array.length == nextIndex) {
      return (R[]) array;
    }
    var newArray = factory.apply(nextIndex);
    System.arraycopy(array, 0, newArray, 0, nextIndex);
    return (R[]) newArray;
  }

  /**
   * Tells whether no elements have been added to the array.
   */
  public boolean isEmpty() {
    return nextIndex == 0;
  }
  
  /**
   * The number of elements added to the array.
   */
  public int length() {
    return nextIndex;
  }

  /**
   * The index of the last element added to the array.
   */
  public int lastIndex() {
    return nextIndex - 1;
  }
}
