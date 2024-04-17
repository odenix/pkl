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

public class JqwikExceptionSupport {

  /**
   * Throw the supplied {@link Throwable}, <em>masked</em> as an unchecked exception.
   *
   * @param t the Throwable to be wrapped
   * @param <T> type of the value to return
   * @return Fake return to make using the method a bit simpler
   */
  public static <T> T throwAsUncheckedException(Throwable t) {
    JqwikExceptionSupport.throwAs(t);

    // Will never get here
    return null;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void throwAs(Throwable t) throws T {
    throw (T) t;
  }

  public static void rethrowIfBlacklisted(Throwable exception) {
    if (exception instanceof OutOfMemoryError) {
      throwAsUncheckedException(exception);
    }
  }

  public static boolean isInstanceOfAny(
      Throwable throwable, Class<? extends Throwable>[] exceptionTypes) {
    return Arrays.stream(exceptionTypes)
        .anyMatch(exceptionType -> exceptionType.isInstance(throwable));
  }
}
