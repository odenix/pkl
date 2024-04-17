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

import org.pkl.core.util.Nullable;

/**
 * The methods in this class mimic the behaviour of {@link java.util.Objects#hash(Object...)} ()}
 * but do not create an array on the way.
 */
public class HashCodeSupport {

  private HashCodeSupport() {}

  public static int hash(@Nullable Object o) {
    return baseHash(o) + 31;
  }

  private static int baseHash(Object o) {
    if (o == null) return 0;
    return o.hashCode();
  }

  public static int hash(@Nullable Object o1, @Nullable Object o2) {
    return 31 * hash(o1) + baseHash(o2);
  }

  public static int hash(@Nullable Object o1, @Nullable Object o2, @Nullable Object o3) {
    return 31 * hash(o1, o2) + baseHash(o3);
  }

  public static int hash(
      @Nullable Object o1, @Nullable Object o2, @Nullable Object o3, @Nullable Object o4) {
    return 31 * hash(o1, o2, o3) + baseHash(o4);
  }

  public static int hash(
      @Nullable Object o1,
      @Nullable Object o2,
      @Nullable Object o3,
      @Nullable Object o4,
      @Nullable Object o5) {
    return 31 * hash(o1, o2, o3, o4) + baseHash(o5);
  }

  public static int hash(
      @Nullable Object o1,
      @Nullable Object o2,
      @Nullable Object o3,
      @Nullable Object o4,
      @Nullable Object o5,
      @Nullable Object o6) {
    return 31 * hash(o1, o2, o3, o4, o5) + baseHash(o6);
  }

  public static int hash(
      @Nullable Object o1,
      @Nullable Object o2,
      @Nullable Object o3,
      @Nullable Object o4,
      @Nullable Object o5,
      @Nullable Object o6,
      @Nullable Object o7) {
    return 31 * hash(o1, o2, o3, o4, o5, o6) + baseHash(o7);
  }

  public static int hash(
      @Nullable Object o1,
      @Nullable Object o2,
      @Nullable Object o3,
      @Nullable Object o4,
      @Nullable Object o5,
      @Nullable Object o6,
      @Nullable Object o7,
      @Nullable Object o8) {
    return 31 * hash(o1, o2, o3, o4, o5, o6, o7) + baseHash(o8);
  }
}
