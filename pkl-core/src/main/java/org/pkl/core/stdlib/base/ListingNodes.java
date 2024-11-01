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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.LoopNode;
import org.pkl.core.ast.PklNode;
import org.pkl.core.ast.lambda.*;
import org.pkl.core.runtime.*;
import org.pkl.core.runtime.VmObjectCursor.CursorOption;
import org.pkl.core.stdlib.ExternalMethod0Node;
import org.pkl.core.stdlib.ExternalMethod1Node;
import org.pkl.core.stdlib.ExternalMethod2Node;
import org.pkl.core.stdlib.ExternalPropertyNode;
import org.pkl.core.util.*;

public final class ListingNodes {
  private ListingNodes() {}

  public abstract static class length extends ExternalPropertyNode {
    @Specialization
    protected long eval(VmListing self) {
      return self.getLength();
    }
  }

  public abstract static class isEmpty extends ExternalPropertyNode {
    @Specialization
    protected boolean eval(VmListing self) {
      return self.isEmpty();
    }
  }

  public abstract static class lastIndex extends ExternalPropertyNode {
    @Specialization
    protected long eval(VmListing self) {
      return self.getLength() - 1;
    }
  }

  public abstract static class getOrNull extends ExternalMethod1Node {
    @Specialization
    protected Object eval(VmListing self, long index) {
      if (index < 0 || index >= self.getLength()) {
        return VmNull.withoutDefault();
      }
      return VmUtils.readMember(self, index);
    }
  }

  public abstract static class isDistinct extends ExternalPropertyNode {
    @Specialization
    protected boolean eval(VmListing self) {
      var seenValues = CollectionUtils.newHashSet();
      for (var cursor = self.elements(CursorOption.ANY_ORDER); cursor.advance(); ) {
        if (!cursor.addValueTo(seenValues)) return false;
      }
      return true;
    }
  }

  public abstract static class isDistinctBy extends ExternalMethod1Node {
    @Child private ApplyVmFunction1Node applyNode = ApplyVmFunction1Node.create();

    @Specialization
    protected boolean eval(VmListing self, VmFunction selector) {
      var seenValues = CollectionUtils.newHashSet();
      for (var cursor = self.elements(CursorOption.ANY_ORDER); cursor.advance(); ) {
        var value = applyNode.execute(selector, cursor.value());
        if (!cursor.addValueTo(seenValues, value)) return false;
      }
      return true;
    }
  }

  public abstract static class distinct extends ExternalPropertyNode {
    @Specialization
    protected VmListing eval(VmListing self) {
      var seenValues = CollectionUtils.newHashSet();
      var builder = new VmObjectBuilder();
      for (var cursor = self.elements(CursorOption.ALL_VALUES); cursor.advance(); ) {
        var value = cursor.value();
        if (cursor.addValueTo(seenValues)) {
          builder.addElement(value);
        }
      }
      return builder.toListing();
    }
  }

  public abstract static class first extends ExternalPropertyNode {
    @Specialization
    protected Object eval(VmListing self) {
      checkNonEmpty(self, this);
      return VmUtils.readMember(self, 0L);
    }
  }

  public abstract static class firstOrNull extends ExternalPropertyNode {
    @Specialization
    protected Object eval(VmListing self) {
      if (self.isEmpty()) {
        return VmNull.withoutDefault();
      }
      return VmUtils.readMember(self, 0L);
    }
  }

  public abstract static class last extends ExternalPropertyNode {
    @Specialization
    protected Object eval(VmListing self) {
      checkNonEmpty(self, this);
      return VmUtils.readMember(self, self.getLength() - 1L);
    }
  }

  public abstract static class lastOrNull extends ExternalPropertyNode {
    @Specialization
    protected Object eval(VmListing self) {
      var length = self.getLength();
      return length == 0 ? VmNull.withoutDefault() : VmUtils.readMember(self, length - 1L);
    }
  }

  public abstract static class single extends ExternalPropertyNode {
    @Specialization
    protected Object eval(VmListing self) {
      checkSingleton(self, this);
      return VmUtils.readMember(self, 0L);
    }
  }

  public abstract static class singleOrNull extends ExternalPropertyNode {
    @Specialization
    protected Object eval(VmListing self) {
      if (self.getLength() != 1) {
        return VmNull.withoutDefault();
      }
      return VmUtils.readMember(self, 0L);
    }
  }

  public abstract static class distinctBy extends ExternalMethod1Node {
    @Child private ApplyVmFunction1Node applyNode = ApplyVmFunction1Node.create();

    @Specialization
    protected VmListing eval(VmListing self, VmFunction selector) {
      var seenValues = CollectionUtils.newHashSet();
      var builder = new VmObjectBuilder();
      for (var cursor = self.elements(CursorOption.ALL_VALUES); cursor.advance(); ) {
        var value = cursor.value();
        var selectorValue = applyNode.execute(selector, value);
        if (cursor.addValueTo(seenValues, selectorValue)) {
          builder.addElement(value);
        }
      }
      return builder.toListing();
    }
  }

  public abstract static class every extends ExternalMethod1Node {
    @Child private ApplyVmFunction1Node applyNode = ApplyVmFunction1Node.create();

    @Specialization
    protected boolean eval(VmListing self, VmFunction predicate) {
      for (var cursor = self.elements(CursorOption.ANY_ORDER); cursor.advance(); ) {
        if (!applyNode.executeBoolean(predicate, cursor.value())) return false;
      }
      return true;
    }
  }

  public abstract static class any extends ExternalMethod1Node {
    @Child private ApplyVmFunction1Node applyNode = ApplyVmFunction1Node.create();

    @Specialization
    protected boolean eval(VmListing self, VmFunction predicate) {
      for (var cursor = self.elements(CursorOption.ANY_ORDER); cursor.advance(); ) {
        if (applyNode.executeBoolean(predicate, cursor.value())) return true;
      }
      return false;
    }
  }

  public abstract static class contains extends ExternalMethod1Node {
    @Specialization
    protected boolean eval(VmListing self, Object element) {
      for (var cursor = self.elements(CursorOption.ANY_ORDER); cursor.advance(); ) {
        if (cursor.valueEquals(element)) return true;
      }
      return false;
    }
  }

  public abstract static class fold extends ExternalMethod2Node {
    @Child private ApplyVmFunction2Node applyLambdaNode = ApplyVmFunction2NodeGen.create();

    @Specialization
    protected Object eval(VmListing self, Object initial, VmFunction function) {
      var result = initial;
      for (var cursor = self.elements(CursorOption.ALL_VALUES); cursor.advance(); ) {
        result = applyLambdaNode.execute(function, result, cursor.value());
      }
      LoopNode.reportLoopCount(this, self.getLength());
      return result;
    }
  }

  public abstract static class foldIndexed extends ExternalMethod2Node {
    @Child private ApplyVmFunction3Node applyLambdaNode = ApplyVmFunction3NodeGen.create();

    @Specialization
    protected Object eval(VmListing self, Object initial, VmFunction function) {
      var result = initial;
      for (var cursor = self.elements(CursorOption.ALL_VALUES); cursor.advance(); ) {
        result = applyLambdaNode.execute(function, cursor.key(), result, cursor.value());
      }
      LoopNode.reportLoopCount(this, self.getLength());
      return result;
    }
  }

  public abstract static class join extends ExternalMethod1Node {
    @Specialization
    protected Object eval(VmListing self, String separator) {
      if (self.isEmpty()) return "";

      var cursor = self.elements(CursorOption.ALL_VALUES);
      cursor.advance();
      var builder = new StringBuilder();
      cursor.appendValueTo(builder);
      while (cursor.advance()) {
        cursor.appendValueTo(builder, separator);
        cursor.appendValueTo(builder);
      }
      LoopNode.reportLoopCount(this, self.getLength());
      return builder.toString();
    }
  }

  public abstract static class toList extends ExternalMethod0Node {
    @Specialization
    protected VmList eval(VmListing self) {
      var builder = VmList.EMPTY.builder();
      for (var cursor = self.elements(CursorOption.ALL_VALUES); cursor.advance(); ) {
        builder.add(cursor.value());
      }
      return builder.build();
    }
  }

  public abstract static class toSet extends ExternalMethod0Node {
    @Specialization
    protected VmSet eval(VmListing self) {
      var builder = VmSet.EMPTY.builder();
      for (var cursor = self.elements(CursorOption.ALL_VALUES); cursor.advance(); ) {
        builder.add(cursor.value());
      }
      return builder.build();
    }
  }

  private static void checkNonEmpty(VmListing self, PklNode node) {
    if (self.isEmpty()) {
      CompilerDirectives.transferToInterpreter();
      throw new VmExceptionBuilder()
          .evalError("expectedNonEmptyListing")
          .withLocation(node)
          .build();
    }
  }

  private static void checkSingleton(VmListing self, PklNode node) {
    if (self.getLength() != 1) {
      CompilerDirectives.transferToInterpreter();
      throw new VmExceptionBuilder()
          .evalError("expectedSingleElementListing")
          .withLocation(node)
          .build();
    }
  }
}
