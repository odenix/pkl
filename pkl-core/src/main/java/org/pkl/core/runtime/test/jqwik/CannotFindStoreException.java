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

/** Experimental feature. Not ready for public usage yet. */
public class CannotFindStoreException extends RuntimeException {
  public CannotFindStoreException(Object identifier, String retrieverId) {
    super(createMessage(identifier, retrieverId));
  }

  private static String createMessage(Object identifier, String retrieverId) {
    return String.format(
        "Cannot find store with identifier [%s] for [%s]", identifier, retrieverId);
  }
}
