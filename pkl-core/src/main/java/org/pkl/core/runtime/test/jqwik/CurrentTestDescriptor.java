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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CurrentTestDescriptor {

  // Current test descriptors are stored in a stack because one test might invoke others
  // e.g. in JqwikIntegrationTests
  private static final ThreadLocal<List<TestDescriptor>> descriptors =
      ThreadLocal.withInitial(ArrayList::new);

  public static <T> T runWithDescriptor(TestDescriptor currentDescriptor, Supplier<T> executable) {
    push(currentDescriptor);
    try {
      return executable.get();
    } finally {
      TestDescriptor peek = descriptors.get().get(0);
      if (peek == currentDescriptor) {
        pop();
      }
    }
  }

  public static TestDescriptor pop() {
    return descriptors.get().remove(0);
  }

  public static void push(TestDescriptor currentDescriptor) {
    descriptors.get().add(0, currentDescriptor);
  }

  public static boolean isEmpty() {
    return descriptors.get().isEmpty();
  }

  public static TestDescriptor get() {
    if (isEmpty()) {
      String message =
          String.format(
              "The current action must be run on a jqwik thread, i.e. container, property or hook.%n"
                  + "Maybe you spawned off a thread?");
      throw new OutsideJqwikException(message);
    }
    return descriptors.get().get(0);
  }
}
