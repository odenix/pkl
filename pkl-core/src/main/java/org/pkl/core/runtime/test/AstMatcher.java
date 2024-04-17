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
package org.pkl.core.runtime.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.pkl.core.ast.ConstantNode;
import org.pkl.core.ast.ExpressionNode;
import org.pkl.core.ast.expression.ComparisonNode;
import org.pkl.core.ast.expression.InvocationNode;
import org.pkl.core.ast.expression.member.ReadPropertyNode;
import org.pkl.core.ast.expression.primary.CustomThisNode;
import org.pkl.core.ast.expression.primary.ResolveVariableNode;
import org.pkl.core.ast.expression.primary.ThisNode;
import org.pkl.core.ast.type.TypeConstraintNode;
import org.pkl.core.ast.type.UnresolvedTypeNode;
import org.pkl.core.ast.type.UnresolvedTypeNode.UnionOfStringLiterals;
import org.pkl.core.runtime.Identifier;
import org.pkl.core.runtime.VmEvalException;
import org.pkl.core.runtime.VmException;
import org.pkl.core.runtime.VmExceptionBuilder;
import org.pkl.core.util.Nullable;
import org.pkl.core.util.Pair;

final class AstMatcher {
  private AstMatcher() {}

  /**
   * someMemberInvocation this.someMemberInvocation receiverProperty.someMemberInvocation
   * this.receiverProperty.someMemberInvocation
   * enclosingProperty.receiverProperty.someMemberInvocation
   * this.enclosingProperty.receiverProperty.someMemberInvocation
   */
  static @Nullable InvocationNode matchInvocation(
      ExpressionNode node,
      @Nullable Identifier receiverProperty,
      @Nullable Identifier enclosingProperty) {
    if (!(node instanceof InvocationNode)) return null;
    var memberInvocation = (InvocationNode) node;
    var receiverNode = memberInvocation.getReceiverNode();
    if (receiverNode == null) {
      return receiverProperty == null ? memberInvocation : null;
    }
    return isProperty(receiverNode, receiverProperty, enclosingProperty) ? memberInvocation : null;
  }

  static <T> @Nullable T matchConstantArgument(InvocationNode invocation, Class<T> type) {
    var argNodes = invocation.getArgumentNodes();
    if (argNodes.length != 1) return null;
    return matchConstant(argNodes[0], type);
  }

  // this
  // property
  // this.property
  // enclosingProperty.property
  // this.enclosingProperty.property
  static boolean isProperty(
      ExpressionNode node, @Nullable Identifier property, @Nullable Identifier enclosingProperty) {
    if (property == null) {
      return node instanceof ThisNode || node instanceof CustomThisNode;
    }
    if (node instanceof ResolveVariableNode) {
      return enclosingProperty == null && ((ResolveVariableNode) node).getMemberName() == property;
    }
    if (node instanceof ReadPropertyNode) {
      var readNode = (ReadPropertyNode) node;
      if (readNode.getMemberName() != property) return false;
      var receiverNode = readNode.getReceiverNode();
      return isProperty(receiverNode, enclosingProperty, null);
    }
    return false;
  }

  // someMethod(this)
  // someMethod(property)
  // someMethod(this.property)
  // someMethod(enclosingProperty.property)
  // someMethod(this.enclosingProperty.property)
  static boolean isPropertyArgument(
      InvocationNode invocation,
      @Nullable Identifier property,
      @Nullable Identifier enclosingProperty) {
    var argNodes = invocation.getArgumentNodes();
    if (argNodes.length != 1) return false;
    return isProperty(argNodes[0], property, enclosingProperty);
  }

  static <T> @Nullable List<T> matchNConstantArguments(InvocationNode invocation, Class<T> type) {
    var result = new ArrayList<T>();
    for (var argNode : invocation.getArgumentNodes()) {
      var value = matchConstant(argNode, type);
      if (value == null) return null;
      result.add(type.cast(value));
    }
    return result;
  }

  static <T, U> @Nullable Pair<T, U> matchTwoConstantArguments(
      InvocationNode invocation, Class<T> type1, Class<U> type2) {
    var argNodes = invocation.getArgumentNodes();
    if (argNodes.length != 2) return null;
    var value1 = matchConstant(argNodes[0], type1);
    if (value1 == null) return null;
    var value2 = matchConstant(argNodes[1], type2);
    if (value2 == null) return null;
    return Pair.of(type1.cast(value1), type2.cast(value2));
  }

  static @Nullable ComparisonNode matchComparison(
      ExpressionNode node, @Nullable Identifier property, @Nullable Identifier enclosingProperty) {
    if (!(node instanceof ComparisonNode)) return null;
    var comparison = (ComparisonNode) node;
    if (isProperty(comparison.getLeftNode(), property, enclosingProperty)) return comparison;
    if (isProperty(comparison.getRightNode(), property, enclosingProperty)) {
      return ComparisonNodeImpl.flip(comparison);
    }
    return null;
  }

  // note special handling for Double.class
  @SuppressWarnings("unchecked")
  static <T> @Nullable T matchConstant(ExpressionNode node, Class<T> type) {
    if (!(node instanceof ConstantNode)) return null;
    var value = ((ConstantNode) node).getValue();
    if (type == Double.class) {
      if (value.getClass() == Double.class) return (T) value;
      if (value.getClass() == Long.class) return (T) (Double) ((Long) value).doubleValue();
      return null;
    }
    if (!(type.isInstance(value))) return null;
    return type.cast(value);
  }

  static @Nullable Pair<Set<String>, Integer> matchUnionOfStringLiterals(
      UnresolvedTypeNode typeNode) {
    if (!(typeNode instanceof UnionOfStringLiterals)) return null;
    var union = (UnionOfStringLiterals) typeNode;
    return Pair.of(union.getStringLiterals(), union.getDefaultIndex());
  }

  static Integer toInt(@Nullable Long l) {
    if (l == null) return null;
    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
      throw new VmExceptionBuilder()
        .adhocEvalError("Excepted a value of type Int32, but got: " + l)
        .build();
    }
    return l.intValue();
  }
  
  static RangeMatch<Integer> computeInt32RangeConstraint(
    TypeConstraintNode[] constraintNodes,
    @Nullable Identifier property,
    @Nullable Identifier enclosingProperty
  ) {
    var match = computeIntRangeConstraint(constraintNodes, property, enclosingProperty);
    return new RangeMatch<>(toInt(match.exactValue()), toInt(match.minValue()), toInt(match.maxValue()));
  }
  
  static RangeMatch<Long> computeIntRangeConstraint(
      TypeConstraintNode[] constraintNodes,
      @Nullable Identifier property,
      @Nullable Identifier enclosingProperty) {
    var minValue = Long.MIN_VALUE;
    var maxValue = Long.MAX_VALUE;
    Long exactValue = null;
    for (var constraintNode : constraintNodes) {
      var bodyNode = constraintNode.getBodyNode();
      var invocation = AstMatcher.matchInvocation(bodyNode, property, enclosingProperty);
      if (invocation != null) {
        if (invocation.isPropertyInvocation(Identifier.IS_POSITIVE)) {
          minValue = Math.max(minValue, 0);
        }
        if (invocation.isMethodInvocation(Identifier.IS_BETWEEN)) {
          var args = AstMatcher.matchTwoConstantArguments(invocation, Long.class, Long.class);
          if (args != null) {
            minValue = Math.max(minValue, args.first);
            maxValue = Math.min(maxValue, args.second);
          }
        }
        continue;
      }
      var comparison = AstMatcher.matchComparison(bodyNode, property, null);
      if (comparison != null) {
        var value = AstMatcher.matchConstant(comparison.getRightNode(), Long.class);
        if (value != null) {
          switch (comparison.getOperator()) {
            case EQUAL:
              exactValue = value;
              break;
            case NOT_EQUAL:
              break;
            case GREATER_THAN_OR_EQUAL:
              minValue = Math.max(minValue, value);
              break;
            case LESS_THAN_OR_EQUAL:
              maxValue = Math.min(maxValue, value);
              break;
            case LESS_THAN:
              maxValue = Math.min(maxValue, value - 1);
              break;
            case GREATER_THAN:
              minValue = Math.max(minValue, value + 1);
          }
        }
      }
    }
    if (exactValue != null) {
      return new RangeMatch<>(exactValue, null, null);
    }
    if (minValue != Long.MIN_VALUE || maxValue != Long.MAX_VALUE) {
      return new RangeMatch<>(null, minValue, maxValue);
    }
    return new RangeMatch<>(null, null, null);
  }

  static @Nullable RangeMatch<Double> computeFloatRangeConstraint(
      TypeConstraintNode[] constraintNodes,
      @Nullable Identifier property,
      @Nullable Identifier enclosingProperty) {
    var minValue = Double.NEGATIVE_INFINITY;
    var maxValue = Double.POSITIVE_INFINITY;
    Double exactValue = null;
    for (var constraintNode : constraintNodes) {
      var bodyNode = constraintNode.getBodyNode();
      var invocation = AstMatcher.matchInvocation(bodyNode, property, enclosingProperty);
      if (invocation != null) {
        if (invocation.isPropertyInvocation(Identifier.IS_POSITIVE)) {
          minValue = Math.max(minValue, 0);
        }
        if (invocation.isMethodInvocation(Identifier.IS_BETWEEN)) {
          var args = AstMatcher.matchTwoConstantArguments(invocation, Double.class, Double.class);
          if (args != null) {
            minValue = Math.max(minValue, args.first);
            maxValue = Math.min(maxValue, args.second);
            continue;
          }
        }
        continue;
      }
      var comparison = AstMatcher.matchComparison(bodyNode, property, enclosingProperty);
      if (comparison != null) {
        var value = AstMatcher.matchConstant(comparison.getRightNode(), Double.class);
        if (value != null) {
          switch (comparison.getOperator()) {
            case EQUAL:
              exactValue = value;
              break;
            case NOT_EQUAL:
              break;
            case GREATER_THAN_OR_EQUAL:
              minValue = Math.max(minValue, value);
              break;
            case LESS_THAN_OR_EQUAL:
              maxValue = Math.min(maxValue, value);
              break;
            case LESS_THAN:
              maxValue = Math.min(maxValue, Math.nextDown(value));
              break;
            case GREATER_THAN:
              minValue = Math.max(minValue, Math.nextUp(value));
          }
        }
      }
    }
    if (exactValue != null) {
      return new RangeMatch<>(exactValue, null, null);
    }
    if (minValue != Double.NEGATIVE_INFINITY || maxValue != Double.POSITIVE_INFINITY) {
      return new RangeMatch<>(null, minValue, maxValue);
    }
    return null;
  }
}
