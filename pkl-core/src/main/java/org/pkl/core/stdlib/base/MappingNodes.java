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
package org.pkl.core.stdlib.base;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import org.pkl.core.ast.lambda.ApplyVmFunction2Node;
import org.pkl.core.ast.lambda.ApplyVmFunction2NodeGen;
import org.pkl.core.ast.lambda.ApplyVmFunction3Node;
import org.pkl.core.ast.lambda.ApplyVmFunction3NodeGen;
import org.pkl.core.runtime.*;
import org.pkl.core.runtime.VmObjectCursor.CursorOption;
import org.pkl.core.stdlib.ExternalMethod0Node;
import org.pkl.core.stdlib.ExternalMethod1Node;
import org.pkl.core.stdlib.ExternalMethod2Node;
import org.pkl.core.stdlib.ExternalPropertyNode;

public final class MappingNodes {
  private MappingNodes() {}

  public abstract static class isEmpty extends ExternalPropertyNode {
    @Specialization
    protected boolean eval(VmMapping self) {
      return !self.entries(CursorOption.ANY_ORDER, CursorOption.LAZY_REQUIRED).advance();
    }
  }

  public abstract static class length extends ExternalPropertyNode {
    @Specialization
    protected long eval(VmMapping self) {
      return self.getAllKeys().getLength();
    }
  }

  public abstract static class keys extends ExternalPropertyNode {
    @Specialization
    protected VmSet eval(VmMapping self) {
      return self.getAllKeys();
    }
  }

  public abstract static class containsKey extends ExternalMethod1Node {
    @Specialization
    protected boolean eval(VmMapping self, Object key) {
      if (self.hasCachedValue(key)) return true;

      for (VmObject curr = self; curr != null; curr = curr.getParent()) {
        if (curr.hasMember(key)) return true;
      }

      return false;
    }
  }

  public abstract static class containsValue extends ExternalMethod1Node {
    @Specialization
    protected boolean eval(VmMapping self, Object value) {
      for (var cursor = self.entries(CursorOption.ANY_ORDER); cursor.advance(); ) {
        if (cursor.valueEquals(value)) return true;
      }
      return false;
    }
  }

  public abstract static class getOrNull extends ExternalMethod1Node {
    @Child private IndirectCallNode callNode = IndirectCallNode.create();

    @Specialization
    protected Object eval(VmMapping self, Object key) {
      return VmNull.lift(VmUtils.readMemberOrNull(self, key, callNode));
    }
  }

  public abstract static class fold extends ExternalMethod2Node {
    @Child private ApplyVmFunction3Node applyLambdaNode = ApplyVmFunction3NodeGen.create();

    @Specialization
    protected Object eval(VmMapping self, Object initial, VmFunction function) {
      var result = initial;
      for (var cursor = self.entries(); cursor.advance(); ) {
        result = applyLambdaNode.execute(function, result, cursor.key(), cursor.value());
      }
      return result;
    }
  }

  public abstract static class every extends ExternalMethod1Node {
    @Child private ApplyVmFunction2Node applyLambdaNode = ApplyVmFunction2NodeGen.create();

    @Specialization
    protected boolean eval(VmMapping self, VmFunction function) {
      for (var cursor = self.entries(CursorOption.ANY_ORDER); cursor.advance(); ) {
        if (!applyLambdaNode.executeBoolean(function, cursor.key(), cursor.value())) return false;
      }
      return true;
    }
  }

  public abstract static class any extends ExternalMethod1Node {
    @Child private ApplyVmFunction2Node applyLambdaNode = ApplyVmFunction2NodeGen.create();

    @Specialization
    protected boolean eval(VmMapping self, VmFunction function) {
      for (var cursor = self.entries(CursorOption.ANY_ORDER); cursor.advance(); ) {
        if (applyLambdaNode.executeBoolean(function, cursor.key(), cursor.value())) return true;
      }
      return false;
    }
  }

  public abstract static class toMap extends ExternalMethod0Node {
    @Specialization
    protected VmMap eval(VmMapping self) {
      var builder = VmMap.builder();
      for (var cursor = self.entries(); cursor.advance(); ) {
        builder.add(cursor.key(), cursor.value());
      }
      return builder.build();
    }
  }
}
