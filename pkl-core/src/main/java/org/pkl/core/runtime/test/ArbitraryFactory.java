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

import com.oracle.truffle.api.source.SourceSection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.pkl.core.DataSizeUnit;
import org.pkl.core.DurationUnit;
import org.pkl.core.PClassInfo;
import org.pkl.core.Version;
import org.pkl.core.ast.VmModifier;
import org.pkl.core.ast.expression.ComparisonNode.Operator;
import org.pkl.core.ast.expression.InvocationNode;
import org.pkl.core.ast.member.ObjectMember;
import org.pkl.core.ast.type.TypeConstraintNode;
import org.pkl.core.ast.type.TypeNode;
import org.pkl.core.ast.type.TypeNode.BooleanTypeNode;
import org.pkl.core.ast.type.TypeNode.CollectionTypeNode;
import org.pkl.core.ast.type.TypeNode.ConstrainedTypeNode;
import org.pkl.core.ast.type.TypeNode.FinalClassTypeNode;
import org.pkl.core.ast.type.TypeNode.FloatTypeNode;
import org.pkl.core.ast.type.TypeNode.IntTypeNode;
import org.pkl.core.ast.type.TypeNode.ListTypeNode;
import org.pkl.core.ast.type.TypeNode.ListingTypeNode;
import org.pkl.core.ast.type.TypeNode.MapTypeNode;
import org.pkl.core.ast.type.TypeNode.MappingTypeNode;
import org.pkl.core.ast.type.TypeNode.NonFinalClassTypeNode;
import org.pkl.core.ast.type.TypeNode.NullableTypeNode;
import org.pkl.core.ast.type.TypeNode.PairTypeNode;
import org.pkl.core.ast.type.TypeNode.SetTypeNode;
import org.pkl.core.ast.type.TypeNode.StringLiteralTypeNode;
import org.pkl.core.ast.type.TypeNode.StringTypeNode;
import org.pkl.core.ast.type.TypeNode.UnionOfStringLiteralsTypeNode;
import org.pkl.core.ast.type.TypeNode.UnionTypeNode;
import org.pkl.core.ast.type.TypeTestNode;
import org.pkl.core.runtime.BaseModule;
import org.pkl.core.runtime.Identifier;
import org.pkl.core.runtime.VmClass;
import org.pkl.core.runtime.VmDataSize;
import org.pkl.core.runtime.VmDuration;
import org.pkl.core.runtime.VmExceptionBuilder;
import org.pkl.core.runtime.VmLanguage;
import org.pkl.core.runtime.VmList;
import org.pkl.core.runtime.VmListing;
import org.pkl.core.runtime.VmMap;
import org.pkl.core.runtime.VmMapping;
import org.pkl.core.runtime.VmNull;
import org.pkl.core.runtime.VmPair;
import org.pkl.core.runtime.VmSet;
import org.pkl.core.runtime.VmTyped;
import org.pkl.core.runtime.VmUtils;
import org.pkl.core.runtime.test.jqwik.Arbitraries;
import org.pkl.core.runtime.test.jqwik.Arbitrary;
import org.pkl.core.runtime.test.jqwik.Builders;
import org.pkl.core.runtime.test.jqwik.Combinators;
import org.pkl.core.runtime.test.jqwik.DoubleArbitrary;
import org.pkl.core.runtime.test.jqwik.ListArbitrary;
import org.pkl.core.runtime.test.jqwik.LongArbitrary;
import org.pkl.core.runtime.test.jqwik.MapArbitrary;
import org.pkl.core.runtime.test.jqwik.SetArbitrary;
import org.pkl.core.runtime.test.jqwik.SizableArbitrary;
import org.pkl.core.runtime.test.jqwik.StringArbitrary;
import org.pkl.core.runtime.test.jqwik.Tuple;
import org.pkl.core.runtime.test.jqwik.Tuple.Tuple2;
import org.pkl.core.stdlib.VmObjectFactories;
import org.pkl.core.stdlib.VmObjectFactory;
import org.pkl.core.util.EconomicMaps;
import org.pkl.core.util.Nullable;

final class ArbitraryFactory {

  private final VmLanguage vmLanguage = VmLanguage.get(null);
  private final Map<VmClass, Arbitrary<VmTyped>> typedObjectArbitraries = new HashMap<>();
  private final Arbitrary<String> durationUnitArbitrary =
      Arbitraries.of(
          Stream.of(DurationUnit.values())
              .map(DurationUnit::getSymbol)
              .collect(Collectors.toList()));
  private final Arbitrary<String> dataSizeUnitArbitrary =
      Arbitraries.of(
          Stream.of(DataSizeUnit.values())
              .map(DataSizeUnit::getSymbol)
              .collect(Collectors.toList()));

  Arbitrary<?> getArbitrary(TypeNode typeNode) {
    return getArbitrary(typeNode, new TypeConstraintNode[0], null);
  }

  private Arbitrary<?> getArbitrary(
      TypeNode typeNode, TypeConstraintNode[] constraintNodes, @Nullable Identifier propertyName) {
    var result = getGenericConstraintsArbitrary(constraintNodes, propertyName, Object.class);
    if (result != null) return result;

    if (typeNode instanceof StringTypeNode) {
      return applyStringConstraints(Arbitraries.strings(), constraintNodes, propertyName);
    }
    if (typeNode instanceof StringLiteralTypeNode) {
      return Arbitraries.just(((StringLiteralTypeNode) typeNode).getLiteral());
    }
    if (typeNode instanceof UnionOfStringLiteralsTypeNode) {
      return Arbitraries.of(((UnionOfStringLiteralsTypeNode) typeNode).getStringLiterals());
    }
    if (typeNode instanceof IntTypeNode) {
      return applyIntConstraints(Arbitraries.longs(), constraintNodes, propertyName);
    }
    if (typeNode instanceof FloatTypeNode) {
      return applyFloatConstraints(Arbitraries.doubles(), constraintNodes, propertyName);
    }
    if (typeNode instanceof BooleanTypeNode) {
      return Arbitraries.of(false, true);
    }
    if (typeNode instanceof PairTypeNode) {
      var firstTypeNode = ((PairTypeNode) typeNode).getFirstTypeNode();
      var secondTypeNode = ((PairTypeNode) typeNode).getSecondTypeNode();
      return Combinators.combine(getArbitrary(firstTypeNode), getArbitrary(secondTypeNode))
          .as(VmPair::new);
    }
    if (typeNode instanceof ListTypeNode) {
      var elementTypeNode = ((ListTypeNode) typeNode).getElementTypeNode();
      var elementArbitrary = getArbitrary(elementTypeNode);
      return applyListConstraints(elementArbitrary.list(), constraintNodes, propertyName)
          .map(VmList::create);
    }
    if (typeNode instanceof SetTypeNode) {
      var elementTypeNode = ((SetTypeNode) typeNode).getElementTypeNode();
      var elementArbitrary = getArbitrary(elementTypeNode);
      return applySetConstraints(elementArbitrary.set(), constraintNodes, propertyName)
          .map(VmSet::create);
    }
    if (typeNode instanceof CollectionTypeNode) {
      var elementTypeNode = ((CollectionTypeNode) typeNode).getElementTypeNode();
      var elementArbitrary = getArbitrary(elementTypeNode);
      return Arbitraries.oneOf(
          applyListConstraints(elementArbitrary.list(), constraintNodes, propertyName)
              .map(VmList::create),
          applySetConstraints(elementArbitrary.set(), constraintNodes, propertyName)
              .map(VmSet::create));
    }
    if (typeNode instanceof MapTypeNode) {
      var keyTypeNode = ((MapTypeNode) typeNode).getKeyTypeNode();
      var valueTypeNode = ((MapTypeNode) typeNode).getValueTypeNode();
      var mapArbitrary =
          applyMapConstraints(
              Arbitraries.maps(getArbitrary(keyTypeNode), getArbitrary(valueTypeNode)),
              constraintNodes,
              propertyName);
      return mapArbitrary.map(
          map -> {
            var builder = VmMap.builder();
            for (var entry : map.entrySet()) {
              builder.add(entry.getKey(), entry.getValue());
            }
            return builder.build();
          });
    }
    if (typeNode instanceof ListingTypeNode) {
      var elementTypeNode = ((ListingTypeNode) typeNode).getValueTypeNode();
      var listArbitrary =
          applyListConstraints(getArbitrary(elementTypeNode).list(), constraintNodes, propertyName);
      return listArbitrary.map(
          list -> {
            var members = EconomicMaps.<Object, ObjectMember>create();
            for (var i = 0; i < list.size(); i++) {
              EconomicMaps.put(
                  members,
                  (long) i,
                  VmUtils.createSyntheticObjectElement(String.valueOf(i), list.get(i)));
            }
            return new VmListing(
                VmUtils.createEmptyMaterializedFrame(),
                BaseModule.getListingClass().getPrototype(),
                members,
                list.size());
          });
    }
    if (typeNode instanceof MappingTypeNode) {
      var keyTypeNode = ((MappingTypeNode) typeNode).getKeyTypeNode();
      assert keyTypeNode != null;
      var valueTypeNode = ((MappingTypeNode) typeNode).getValueTypeNode();
      var mapArbitrary =
          applyMapConstraints(
              Arbitraries.maps(getArbitrary(keyTypeNode), getArbitrary(valueTypeNode)),
              constraintNodes,
              propertyName);
      return mapArbitrary.map(
          map -> {
            var members = EconomicMaps.<Object, ObjectMember>create();
            for (var entry : map.entrySet()) {
              EconomicMaps.put(
                  members,
                  entry.getKey(),
                  VmUtils.createSyntheticObjectEntry("", entry.getValue()));
            }
            return new VmMapping(
                VmUtils.createEmptyMaterializedFrame(),
                BaseModule.getMappingClass().getPrototype(),
                members);
          });
    }
    if (typeNode instanceof NullableTypeNode) {
      var elementTypeNode = ((NullableTypeNode) typeNode).getElementTypeNode();
      return Arbitraries.frequencyOf(
          Tuple.of(9, getArbitrary(elementTypeNode)),
          Tuple.of(
              1,
              Arbitraries.just(
                  VmNull.withDefault(
                      elementTypeNode.createDefaultValue(
                          vmLanguage, VmUtils.unavailableSourceSection(), "default")))));
    }
    if (typeNode instanceof FinalClassTypeNode) {
      var vmClass = typeNode.getVmClass();
      assert vmClass != null;
      var classInfo = vmClass.getPClassInfo();
      if (classInfo == PClassInfo.Duration) {
        var valueArbitrary =
            applyGenericConstraints(
                applyFloatConstraints(Arbitraries.doubles(), constraintNodes, Identifier.VALUE),
                constraintNodes,
                Identifier.VALUE,
                Double.class);
        var unitArbitrary =
            applyGenericConstraints(
                durationUnitArbitrary, constraintNodes, Identifier.VALUE, String.class);
        return Combinators.combine(valueArbitrary, unitArbitrary)
            .as(
                (value, unitString) -> {
                  var unit = DurationUnit.parse(unitString);
                  assert unit != null;
                  return new VmDuration(value, unit);
                });
      }
      if (classInfo == PClassInfo.DataSize) {
        var valueArbitrary =
            applyGenericConstraints(
                applyFloatConstraints(Arbitraries.doubles(), constraintNodes, Identifier.VALUE),
                constraintNodes,
                Identifier.VALUE,
                Double.class);
        var unitArbitrary =
            applyGenericConstraints(
                dataSizeUnitArbitrary, constraintNodes, Identifier.VALUE, String.class);
        return Combinators.combine(valueArbitrary, unitArbitrary)
            .as(
                (value, unitString) -> {
                  var unit = DataSizeUnit.parse(unitString);
                  assert unit != null;
                  return new VmDataSize(value, unit);
                });
      }
      if (classInfo == PClassInfo.Version) {
        return Combinators.combine(
                Arbitraries.integers(), Arbitraries.integers(), Arbitraries.integers())
            .as(
                (major, minor, patch) ->
                    VmObjectFactories.versionFactory.create(
                        new Version(major, minor, patch, null, null)));
      }
      return getTypedObjectArbitrary(vmClass, constraintNodes, typeNode.getSourceSection());
    }
    if (typeNode instanceof NonFinalClassTypeNode) {
      var vmClass = typeNode.getVmClass();
      assert vmClass != null;
      return getTypedObjectArbitrary(vmClass, constraintNodes, typeNode.getSourceSection());
    }
    var vmTypeAlias = typeNode.getVmTypeAlias();
    if (vmTypeAlias != null) {
      return getArbitrary(vmTypeAlias.getTypeNode());
    }
    if (typeNode instanceof ConstrainedTypeNode) {
      var constrainedNode = (ConstrainedTypeNode) typeNode;
      return getArbitrary(
          constrainedNode.getChildNode(), constrainedNode.getConstraintNodes(), null);
    }
    if (typeNode instanceof UnionTypeNode) {
      var elementTypeNodes = ((UnionTypeNode) typeNode).getElementTypeNodes();
      return Arbitraries.oneOf(
          Stream.of(elementTypeNodes).map(this::getArbitrary).collect(Collectors.toList()));
    }
    throw new VmExceptionBuilder()
        .adhocEvalError(
            "Property parameter type "
                + typeNode.getSourceSection().getCharacters()
                + " is not supported.")
        .build();
  }

  private <T> @Nullable Arbitrary<? extends T> getGenericConstraintsArbitrary(
      TypeConstraintNode[] constraintNodes, @Nullable Identifier propertyName, Class<T> type) {
    for (var constraintNode : constraintNodes) {
      var bodyNode = constraintNode.getBodyNode();
      var comparison = AstMatcher.matchComparison(bodyNode, propertyName, null);
      if (comparison != null) {
        if (comparison.getOperator() == Operator.EQUAL) {
          // this == ...
          // property == ...
          if (AstMatcher.isProperty(comparison.getLeftNode(), propertyName, null)) {
            var constant = AstMatcher.matchConstant(comparison.getRightNode(), type);
            if (constant != null) {
              return Arbitraries.just(constant);
            }
          }
        }
        continue;
      }
      if (bodyNode instanceof InvocationNode) {
        var invocation = (InvocationNode) bodyNode;
        if (invocation.isMethodInvocation(Identifier.CONTAINS)) {
          // Set/List(literal1, literal2, ...).contains(this)
          // Set/List(literal1, literal2, ...).contains(property)
          if (invocation.getReceiverNode() instanceof InvocationNode) {
            var receiverInvocation = (InvocationNode) invocation.getReceiverNode();
            if (receiverInvocation.isMethodInvocation(Identifier.LIST, Identifier.SET)
                && receiverInvocation.getReceiverNode() == null) {
              if (AstMatcher.isPropertyArgument(invocation, propertyName, null)) {
                var constants = AstMatcher.matchNConstantArguments(receiverInvocation, type);
                if (constants != null) {
                  return Arbitraries.of(constants);
                }
              }
            }
          }
        }
        continue;
      }
      if (bodyNode instanceof TypeTestNode && type.isAssignableFrom(String.class)) {
        // this is "foo"|*"bar"|"baz"
        // property is "foo"|*"bar"|"baz"
        var typeTestNode = (TypeTestNode) bodyNode;
        if (AstMatcher.isProperty(typeTestNode.getValueNode(), propertyName, null)) {
          var literals =
              AstMatcher.matchUnionOfStringLiterals(typeTestNode.getUnresolvedTypeNode());
          if (literals != null) {
            int defaultIndex = literals.second;
            if (defaultIndex == -1) {
              //noinspection unchecked
              return (Arbitrary<? extends T>) Arbitraries.of(literals.first);
            }
            var result = new ArrayList<Tuple2<Integer, String>>();
            var index = 0;
            for (var literal : literals.first) {
              var frequency = index++ == defaultIndex ? 3 : 1;
              result.add(Tuple.of(frequency, literal));
            }
            //noinspection unchecked
            return (Arbitrary<? extends T>) Arbitraries.frequency(result);
          }
        }
      }
    }
    return null;
  }

  private Arbitrary<VmTyped> getTypedObjectArbitrary(
      VmClass vmClass, TypeConstraintNode[] constraintNodes, SourceSection sourceSection) {
    if (vmClass.isExternal()) {
      throw new VmExceptionBuilder()
          .adhocEvalError(
              "Cannot generate value of external class "
                  + vmClass.getSimpleName()
                  + " because I haven't been taught about this class.")
          .withSourceSection(sourceSection)
          .build();
    }
    if (vmClass.isAbstract()) {
      var subclasses = findInstantiableSubclasses(vmClass);
      if (subclasses.isEmpty()) {
        throw new VmExceptionBuilder()
            .adhocEvalError(
                "Cannot generate value of abstract class "
                    + vmClass.getSimpleName()
                    + " because no instantiable subclass was found in the same module. ")
            .withSourceSection(sourceSection)
            .build();
      }
      return Arbitraries.of(subclasses)
          .flatMap((clazz) -> getTypedObjectArbitrary(clazz, constraintNodes, sourceSection));
    }
    return typedObjectArbitraries.computeIfAbsent(
        vmClass,
        (clazz) -> {
          var factory = new VmObjectFactory<Object[]>(() -> vmClass);
          var propertyNames = vmClass.getAllRegularPropertyNames();
          var builder = Builders.withBuilder(() -> new Object[propertyNames.size()]);
          var index = 0;
          for (var propertyName : propertyNames) {
            var idx = index++;
            var property = vmClass.getProperty((Identifier) propertyName);
            assert property != null;
            if (property.isConstOrFixed()) {
              continue;
            }
            var name = property.getName();
            var propertyTypeNode = property.getTypeNode();
            var arbitrary =
                propertyTypeNode != null
                    ? getArbitrary(propertyTypeNode.getTypeNode(), constraintNodes, name)
                    : Arbitraries.just("");
            factory.addIndexedProperty(name, idx);
            builder =
                builder
                    .use(arbitrary)
                    .inSetter(
                        (array, value) -> {
                          assert value != null;
                          array[idx] = value;
                        });
          }
          return builder.build(factory::create);
        });
  }

  private List<VmClass> findInstantiableSubclasses(VmClass clazz) {
    var module = clazz.getModule();
    var subclasses = new ArrayList<VmClass>();
    for (var cursor = module.getMembers().getEntries(); cursor.advance(); ) {
      var member = cursor.getValue();
      if (VmModifier.isClass(member.getModifiers())) {
        var candidate = (VmClass) VmUtils.readMember(module, cursor.getKey());
        if (candidate.isInstantiable() && candidate.isSubclassOf(clazz)) subclasses.add(clazz);
      }
    }
    return subclasses;
  }

  private <T> Arbitrary<? extends T> applyGenericConstraints(
      Arbitrary<T> arbitrary,
      TypeConstraintNode[] constraintNodes,
      @Nullable Identifier propertyName,
      Class<T> type) {
    var constraintsArbitrary = getGenericConstraintsArbitrary(constraintNodes, propertyName, type);
    return constraintsArbitrary != null ? constraintsArbitrary : arbitrary;
  }

  private StringArbitrary applyStringConstraints(
      StringArbitrary arbitrary,
      TypeConstraintNode[] constraintNodes,
      @Nullable Identifier propertyName) {
    var minChar = 0;
    var maxChar = Integer.MAX_VALUE;

    for (var constraintNode : constraintNodes) {
      var bodyNode = constraintNode.getBodyNode();
      var memberInvocation = AstMatcher.matchInvocation(bodyNode, propertyName, null);
      if (memberInvocation != null) {
        if (memberInvocation.isPropertyInvocation(Identifier.IS_ASCII)) {
          maxChar = Math.min(maxChar, 127);
        } else if (memberInvocation.isMethodInvocation(Identifier.HAS_CHARS_BETWEEN)) {
          var args =
              AstMatcher.matchTwoConstantArguments(memberInvocation, String.class, String.class);
          if (args != null) {
            minChar = Math.max(minChar, args.first.codePointAt(0));
            maxChar = Math.min(maxChar, args.second.codePointAt(0));
          }
        }
      }
    }
    if (minChar > 0 || maxChar < Integer.MAX_VALUE) {
      // apparently jqwik can only generate strings whose characters are in BMP
      if (minChar > 0xffff) minChar = 0xffff;
      if (maxChar > 0xffff) maxChar = 0xffff;
      arbitrary = arbitrary.withCharRange((char) minChar, (char) maxChar);
    }

    var lengthMatch =
      AstMatcher.computeInt32RangeConstraint(constraintNodes, Identifier.LENGTH, propertyName);
    if (lengthMatch.exactValue() != null) {
      arbitrary = arbitrary.ofLength(lengthMatch.exactValue());
    } else {
      if (lengthMatch.minValue() != null) {
        arbitrary = arbitrary.ofMinLength(lengthMatch.minValue());
      }
      if (lengthMatch.maxValue() != null) {
        arbitrary = arbitrary.ofMaxLength(lengthMatch.maxValue());
      }
    }
    
    return arbitrary;
  }

  private DoubleArbitrary applyFloatConstraints(
      DoubleArbitrary arbitrary,
      TypeConstraintNode[] constraintNodes,
      @Nullable Identifier propertyName) {
    var rangeMatch = AstMatcher.computeFloatRangeConstraint(constraintNodes, propertyName, null);
    if (rangeMatch == null) return arbitrary;
    // should have already been handled by applyGenericConstraints
    assert rangeMatch.exactValue() == null;
    if (rangeMatch.minValue() != null) {
      arbitrary = arbitrary.greaterOrEqual(rangeMatch.minValue());
    }
    if (rangeMatch.maxValue() != null) {
      arbitrary = arbitrary.lessOrEqual(rangeMatch.maxValue());
    }
    return arbitrary;
  }

  private LongArbitrary applyIntConstraints(
      LongArbitrary arbitrary,
      TypeConstraintNode[] constraintNodes,
      @Nullable Identifier propertyName) {
    var rangeMatch = AstMatcher.computeIntRangeConstraint(constraintNodes, propertyName, null);
    // should have already been handled by applyGenericConstraints
    assert rangeMatch.exactValue() == null; 
    if (rangeMatch.minValue() != null) {
      arbitrary = arbitrary.greaterOrEqual(rangeMatch.minValue());
    }
    if (rangeMatch.maxValue() != null) {
      arbitrary = arbitrary.lessOrEqual(rangeMatch.maxValue());
    }
    return arbitrary;
  }

  private ListArbitrary<?> applyListConstraints(
      ListArbitrary<?> arbitrary,
      TypeConstraintNode[] constraintNodes,
      @Nullable Identifier propertyName) {
    return applyLengthConstraints(arbitrary, constraintNodes, propertyName);
  }

  private SetArbitrary<?> applySetConstraints(
      SetArbitrary<?> arbitrary,
      TypeConstraintNode[] constraintNodes,
      @Nullable Identifier propertyName) {
    return applyLengthConstraints(arbitrary, constraintNodes, propertyName);
  }

  private MapArbitrary<?, ?> applyMapConstraints(
      MapArbitrary<?, ?> arbitrary,
      TypeConstraintNode[] constraintNodes,
      @Nullable Identifier propertyName) {
    return applyLengthConstraints(arbitrary, constraintNodes, propertyName);
  }

  @SuppressWarnings({"unchecked"})
  private <T extends SizableArbitrary<?>> T applyLengthConstraints(
      T arbitrary, TypeConstraintNode[] constraintNodes, @Nullable Identifier propertyName) {
    var rangeMatch =
        AstMatcher.computeInt32RangeConstraint(constraintNodes, Identifier.LENGTH, propertyName);
    if (rangeMatch.exactValue() != null) {
      arbitrary = (T) arbitrary.ofSize(rangeMatch.exactValue());
    } else {
      if (rangeMatch.minValue() != null) {
        arbitrary = (T) arbitrary.ofMinSize(rangeMatch.minValue());
      }
      if (rangeMatch.maxValue() != null) {
        arbitrary = (T) arbitrary.ofMaxSize(rangeMatch.maxValue());
      }
    }
    return arbitrary;
  }
}
