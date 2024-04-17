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
package org.pkl.core.ast.expression;

import org.pkl.core.ast.ExpressionNode;
import org.pkl.core.runtime.Identifier;
import org.pkl.core.util.Nullable;

public interface InvocationNode {
  @Nullable
  ExpressionNode getReceiverNode();

  Identifier getMemberName();

  ExpressionNode[] getArgumentNodes();

  boolean isPropertyInvocation();

  default boolean isPropertyInvocation(Identifier memberName) {
    return isPropertyInvocation() && getMemberName() == memberName;
  }

  default boolean isPropertyInvocation(Identifier... memberNames) {
    if (!isPropertyInvocation()) return false;
    for (var name : memberNames) {
      if (name == getMemberName()) return true;
    }
    return false;
  }

  default boolean isMethodInvocation() {
    return !isPropertyInvocation();
  }

  default boolean isMethodInvocation(Identifier memberName) {
    return isMethodInvocation() && getMemberName() == memberName;
  }

  default boolean isMethodInvocation(Identifier... memberNames) {
    if (!isMethodInvocation()) return false;
    for (var name : memberNames) {
      if (name == getMemberName()) return true;
    }
    return false;
  }
}
