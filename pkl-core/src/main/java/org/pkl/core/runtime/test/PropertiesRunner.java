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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;
import org.pkl.core.Duration;
import org.pkl.core.ast.member.FunctionNode;
import org.pkl.core.runtime.Identifier;
import org.pkl.core.runtime.VmConstraintViolatedException;
import org.pkl.core.runtime.VmDuration;
import org.pkl.core.runtime.VmException;
import org.pkl.core.runtime.VmExceptionBuilder;
import org.pkl.core.runtime.VmFunction;
import org.pkl.core.runtime.VmListing;
import org.pkl.core.runtime.VmMapping;
import org.pkl.core.runtime.VmNull;
import org.pkl.core.runtime.VmObject;
import org.pkl.core.runtime.VmTyped;
import org.pkl.core.runtime.VmUtils;
import org.pkl.core.runtime.test.TestResults.Failure;
import org.pkl.core.runtime.test.jqwik.JqwikSession;

final class PropertiesRunner extends AbstractRunner {
  private final ArbitraryFactory arbitraryFactory = new ArbitraryFactory();
  private boolean sessionStarted;

  void run(VmTyped testModule, TestResults results) {
    if (!sessionStarted) {
      JqwikSession.start();
      sessionStarted = true;
    }
    try {
      var properties = VmUtils.readMember(testModule, Identifier.PROPERTIES);
      if (properties instanceof VmNull) return;
      var settings = (VmObject) VmUtils.readMember(testModule, Identifier.SETTINGS);
      var propertiesSettings = (VmObject) VmUtils.readMember(settings, Identifier.PROPERTIES);
      var tries = ((Long) VmUtils.readMember(propertiesSettings, Identifier.TRIES)).intValue();
      var maxDiscardRatio =
          (double) VmUtils.readMember(propertiesSettings, Identifier.MAX_DISCARD_RATIO);
      var maxShrinkTimePkl =
          (VmDuration) VmUtils.readMember(propertiesSettings, Identifier.MAX_SHRINK_TIME);
      var maxShrinkTime =
          new Duration(maxShrinkTimePkl.getValue(), maxShrinkTimePkl.getUnit()).inWholeNanos();
      var seed = VmNull.unwrap(VmUtils.readMember(propertiesSettings, Identifier.SEED));
      var random = seed != null ? new Random((long) seed) : new Random();
      var propertiesMapping = (VmMapping) properties;
      propertiesMapping.forceAndIterateMemberValues(
          (groupKey, groupMember, groupValue) -> {
            var result = results.newResult(String.valueOf(groupKey));
            var groupFunction = (VmFunction) groupValue;
            var functionNode = (FunctionNode) groupFunction.getCallTarget().getRootNode();
            var parameterTypeNode = functionNode.getParameterTypeNodes()[0];
            var arbitrary = arbitraryFactory.getArbitrary(parameterTypeNode);
            var generator = arbitrary.generator(tries, true);
            var discardedTries = 0;
            var maxDiscardedTries = Math.round(tries * maxDiscardRatio);
            for (var i = 0; i < tries; i++) {
              var shrinkable = generator.next(random);
              var argument = shrinkable.value();
              VmListing groupListing;
              try {
                groupListing = (VmListing) groupFunction.apply(argument);
              } catch (VmConstraintViolatedException e) {
                discardedTries++;
                if (discardedTries >= maxDiscardedTries) {
                  result.addFailure(Failure.buildPropertyGenerationFailure());
                  break;
                }
                continue;
              }
              groupListing.forceAndIterateMemberValues(
                  ((propertyIndex, propertyMember, propertyValue) -> {
                    assert propertyValue instanceof Boolean;
                    if (propertyValue == Boolean.FALSE) {
                      var curr = shrinkable;
                      var currFalsified = shrinkable;
                      var startTime = System.nanoTime();
                      while (true) {
                        var currDistance = curr.distance();
                        var next =
                            curr.shrink()
                                .filter(shrunk -> shrunk.distance().compareTo(currDistance) <= 0)
                                .filter(
                                    shrunk -> {
                                      var shrunkValue = shrunk.value();
                                      VmListing shrunkGroupListing;
                                      try {
                                        shrunkGroupListing =
                                            (VmListing) groupFunction.apply(shrunkValue);
                                      } catch (VmConstraintViolatedException e) {
                                        return false;
                                      }
                                      var testResult =
                                          VmUtils.readMember(shrunkGroupListing, propertyIndex);
                                      return testResult == Boolean.FALSE;
                                    })
                                .findAny();
                        if (next.isPresent()) {
                          curr = next.get();
                          currFalsified = curr;
                        } else {
                          var next2 =
                              curr.shrink()
                                  .filter(shrunk -> shrunk.distance().compareTo(currDistance) <= 0)
                                  .findAny();
                          if (next2.isEmpty()) break;
                          curr = next2.get();
                        }
                        if (System.nanoTime() - startTime >= maxShrinkTime) break;
                      }
                      var explanation = new StringBuilder();
                      var descriptor = functionNode.getFrameDescriptor();
                      explanation
                          .append("Falsified for ")
                          .append(descriptor.getSlotName(0))
                          .append(" = ")
                          .append(renderAsPcf(currFalsified.value()));
                      result.addFailure(
                          Failure.buildPropertyFailure(
                              propertyMember.getSourceSection(),
                              getDisplayUri(propertyMember),
                              explanation.toString()));
                      return false;
                    }
                    return true;
                  }));
              // TODO: should this be called before shrinking?
              JqwikSession.finishTry();
              if (result.isFailure()) break;
            }
            return true;
          });
    } catch (Exception e) {
      if (e instanceof VmException) throw e;
      var writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      throw new VmExceptionBuilder().adhocEvalError(writer.toString()).withCause(e).build();
    }
  }

  void close() {
    if (sessionStarted) {
      JqwikSession.finish();
      sessionStarted = false;
    }
  }
}
