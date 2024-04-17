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
import java.util.function.*;
import java.util.stream.*;

public class JqwikStreamSupport {

  /**
   * From https://stackoverflow.com/a/46230233/32352
   *
   * @param leftStream left
   * @param rightStream right
   * @param combiner combine
   * @param <L> left type
   * @param <R> right type
   * @param <T> result type
   * @return a zipped stream
   */
  public static <L, R, T> Stream<T> zip(
      Stream<L> leftStream, Stream<R> rightStream, BiFunction<L, R, T> combiner) {
    Spliterator<L> lefts = leftStream.spliterator();
    Spliterator<R> rights = rightStream.spliterator();
    return StreamSupport.stream(
        new Spliterators.AbstractSpliterator<T>(
            Long.min(lefts.estimateSize(), rights.estimateSize()),
            lefts.characteristics() & rights.characteristics()) {
          @Override
          public boolean tryAdvance(Consumer<? super T> action) {
            return lefts.tryAdvance(
                left ->
                    rights.tryAdvance(
                        right -> {
                          T combinedValue = combiner.apply(left, right);
                          if (combinedValue == null) {
                            return;
                          }
                          action.accept(combinedValue);
                        }));
          }
        },
        leftStream.isParallel() || rightStream.isParallel());
  }

  @SafeVarargs
  public static <T> Stream<T> concat(Stream<T>... streams) {
    // See discussion in https://github.com/jqwik-team/jqwik/issues/526 why StreamConcatenation is
    // used
    return StreamConcatenation.concat(streams);
  }

  @SuppressWarnings("unchecked")
  public static <T> Stream<T> concat(List<Stream<T>> streams) {
    return concat(streams.toArray(new Stream[0]));
  }
}
