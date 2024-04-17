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

/**
 * Define how long a resource, e.g. the value in a {@linkplain Store} with the same identifier, will
 * live:
 *
 * <ul>
 *   <li>For the whole test run
 *   <li>For the currently running property
 *   <li>For the currently running try
 * </ul>
 *
 * Any hook or collection of hooks can use this enum to allow the specification of the lifespan of
 * resources from which it is abstracting.
 *
 * @see Store
 */
public enum Lifespan {

  /** Live for the whole test run */
  RUN,

  /** Live until the currently running property is finished */
  PROPERTY,

  /** Live for a single try */
  TRY
}
