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

import org.pkl.core.ast.ExpressionNode;
import org.pkl.core.ast.expression.ComparisonNode;

final class ComparisonNodeImpl implements ComparisonNode {
  private final Operator operator;
  private final ExpressionNode leftNode;
  private final ExpressionNode rightNode;

  ComparisonNodeImpl(Operator operator, ExpressionNode leftNode, ExpressionNode rightNode) {
    this.operator = operator;
    this.leftNode = leftNode;
    this.rightNode = rightNode;
  }

  static ComparisonNode flip(ComparisonNode node) {
    return new ComparisonNodeImpl(
        flipOperator(node.getOperator()), node.getRightNode(), node.getLeftNode());
  }

  @Override
  public Operator getOperator() {
    return operator;
  }

  @Override
  public ExpressionNode getLeftNode() {
    return leftNode;
  }

  @Override
  public ExpressionNode getRightNode() {
    return rightNode;
  }

  private static Operator flipOperator(Operator operator) {
    switch (operator) {
      case EQUAL:
        return Operator.EQUAL;
      case NOT_EQUAL:
        return Operator.NOT_EQUAL;
      case LESS_THAN:
        return Operator.GREATER_THAN;
      case GREATER_THAN:
        return Operator.LESS_THAN;
      case LESS_THAN_OR_EQUAL:
        return Operator.GREATER_THAN_OR_EQUAL;
      case GREATER_THAN_OR_EQUAL:
        return Operator.LESS_THAN_OR_EQUAL;
    }
    throw new AssertionError("unreachable");
  }
}
