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

public class JqwikStringSupport {
  public static String displayString(Object object) {
    if (object == null) return "null";
    if (object instanceof Class) {
      return ((Class) object).getName();
    }
    if (object instanceof Collection) {
      @SuppressWarnings("unchecked")
      Collection<Object> collection = (Collection<Object>) object;
      String elements =
          collection.stream()
              .map(JqwikStringSupport::displayString)
              .collect(Collectors.joining(", "));
      return String.format("[%s]", elements);
    }
    if (object.getClass().isArray()) {
      if (object.getClass().getComponentType().isPrimitive()) {
        return nullSafeToString(object);
      }
      Object[] array = (Object[]) object;
      String elements =
          Arrays.stream(array)
              .map(JqwikStringSupport::displayString)
              .collect(Collectors.joining(", "));
      return String.format("%s{%s}", object.getClass().getSimpleName(), elements);
    }
    if (String.class.isAssignableFrom(object.getClass())) {
      return String.format("\"%s\"", replaceUnrepresentableCharacters(object.toString()));
    }
    return replaceUnrepresentableCharacters(object.toString());
  }

  private static String replaceUnrepresentableCharacters(String aString) {
    return aString.replace('\u0000', '\ufffd');
  }

  private static String nullSafeToString(Object obj) {
    if (obj == null) {
      return "null";
    }

    try {
      if (obj.getClass().isArray()) {
        if (obj.getClass().getComponentType().isPrimitive()) {
          if (obj instanceof boolean[]) {
            return Arrays.toString((boolean[]) obj);
          }
          if (obj instanceof char[]) {
            return Arrays.toString((char[]) obj);
          }
          if (obj instanceof short[]) {
            return Arrays.toString((short[]) obj);
          }
          if (obj instanceof byte[]) {
            return Arrays.toString((byte[]) obj);
          }
          if (obj instanceof int[]) {
            return Arrays.toString((int[]) obj);
          }
          if (obj instanceof long[]) {
            return Arrays.toString((long[]) obj);
          }
          if (obj instanceof float[]) {
            return Arrays.toString((float[]) obj);
          }
          if (obj instanceof double[]) {
            return Arrays.toString((double[]) obj);
          }
        }
        return Arrays.deepToString((Object[]) obj);
      }

      // else
      return obj.toString();
    } catch (Throwable throwable) {
      JqwikExceptionSupport.rethrowIfBlacklisted(throwable);
      return defaultToString(obj);
    }
  }

  private static String defaultToString(Object obj) {
    if (obj == null) {
      return "null";
    }

    return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
  }
}
