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

import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.pkl.core.util.Nullable;

/**
 * Typed Tuples are very convenient containers to have, especially in the context of generating
 * dependent values. It's a shame Java does not have them by default.
 */
public interface Tuple extends Serializable, Cloneable {

  int size();

  default List<@Nullable Object> items() {
    return Collections.emptyList();
  }

  default String itemsToString() {
    String items = items().stream().map(Objects::toString).collect(Collectors.joining(", "));
    return String.format("(%s)", items);
  }

  static Tuple0 of() {
    return new Tuple0();
  }

  static Tuple0 empty() {
    return Tuple.of();
  }

  static <@Nullable T1> Tuple1<T1> of(T1 v1) {
    return new Tuple1<>(v1);
  }

  static <@Nullable T1, @Nullable T2> Tuple2<T1, T2> of(T1 v1, T2 v2) {
    return new Tuple2<>(v1, v2);
  }

  static <@Nullable T1, @Nullable T2, @Nullable T3> Tuple3<T1, T2, T3> of(T1 v1, T2 v2, T3 v3) {
    return new Tuple3<>(v1, v2, v3);
  }

  static <@Nullable T1, @Nullable T2, @Nullable T3, @Nullable T4> Tuple4<T1, T2, T3, T4> of(
      T1 v1, T2 v2, T3 v3, T4 v4) {
    return new Tuple4<>(v1, v2, v3, v4);
  }

  class Tuple0 implements Tuple {
    @Override
    public int size() {
      return 0;
    }

    @Override
    public int hashCode() {
      return 42;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) return true;
      return o != null && getClass() == o.getClass();
    }

    @Override
    public String toString() {
      return itemsToString();
    }
  }

  class Tuple1<@Nullable T1> extends Tuple0 {
    final T1 v1;

    private Tuple1(@Nullable T1 v1) {
      super();
      this.v1 = v1;
    }

    @Override
    public int size() {
      return 1;
    }

    public T1 get1() {
      return v1;
    }

    @Override
    public List<Object> items() {
      return Arrays.asList(get1());
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Tuple1<?> tuple = (Tuple1<?>) o;
      return Objects.equals(v1, tuple.v1);
    }

    @Override
    public int hashCode() {
      return HashCodeSupport.hash(v1);
    }

    @Override
    public String toString() {
      return itemsToString();
    }
  }

  class Tuple2<@Nullable T1, @Nullable T2> extends Tuple1<T1> {
    final T2 v2;

    private Tuple2(T1 v1, T2 v2) {
      super(v1);
      this.v2 = v2;
    }

    @Override
    public int size() {
      return 2;
    }

    public T2 get2() {
      return v2;
    }

    @Override
    public List<Object> items() {
      return Arrays.asList(get1(), get2());
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Tuple2<?, ?> tuple = (Tuple2<?, ?>) o;
      return Objects.equals(v1, tuple.v1) //
          && Objects.equals(v2, tuple.v2);
    }

    @Override
    public int hashCode() {
      return HashCodeSupport.hash(v1, v2);
    }
  }

  class Tuple3<@Nullable T1, @Nullable T2, @Nullable T3> extends Tuple2<T1, T2> {
    final T3 v3;

    private Tuple3(T1 v1, T2 v2, T3 v3) {
      super(v1, v2);
      this.v3 = v3;
    }

    @Override
    public int size() {
      return 3;
    }

    public T3 get3() {
      return v3;
    }

    @Override
    public List<Object> items() {
      return Arrays.asList(get1(), get2(), get3());
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Tuple3<?, ?, ?> tuple = (Tuple3<?, ?, ?>) o;
      return Objects.equals(v1, tuple.v1) //
          && Objects.equals(v2, tuple.v2) //
          && Objects.equals(v3, tuple.v3);
    }

    @Override
    public int hashCode() {
      return HashCodeSupport.hash(v1, v2, v3);
    }
  }

  class Tuple4<@Nullable T1, @Nullable T2, @Nullable T3, @Nullable T4> extends Tuple3<T1, T2, T3> {
    final T4 v4;

    private Tuple4(T1 v1, T2 v2, T3 v3, T4 v4) {
      super(v1, v2, v3);
      this.v4 = v4;
    }

    @Override
    public int size() {
      return 4;
    }

    public T4 get4() {
      return v4;
    }

    @Override
    public List<Object> items() {
      return Arrays.asList(get1(), get2(), get3(), get4());
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Tuple4<?, ?, ?, ?> tuple = (Tuple4<?, ?, ?, ?>) o;
      return Objects.equals(v1, tuple.v1) //
          && Objects.equals(v2, tuple.v2) //
          && Objects.equals(v3, tuple.v3) //
          && Objects.equals(v4, tuple.v4);
    }

    @Override
    public int hashCode() {
      return HashCodeSupport.hash(v1, v2, v3, v4);
    }
  }
}
