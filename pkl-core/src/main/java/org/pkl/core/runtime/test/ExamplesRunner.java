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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.pkl.core.module.ModuleKeys;
import org.pkl.core.runtime.BaseModule;
import org.pkl.core.runtime.Identifier;
import org.pkl.core.runtime.ModuleInfo;
import org.pkl.core.runtime.VmDynamic;
import org.pkl.core.runtime.VmExceptionBuilder;
import org.pkl.core.runtime.VmLanguage;
import org.pkl.core.runtime.VmListing;
import org.pkl.core.runtime.VmMapping;
import org.pkl.core.runtime.VmNull;
import org.pkl.core.runtime.VmObjectLike;
import org.pkl.core.runtime.VmTyped;
import org.pkl.core.runtime.VmUtils;
import org.pkl.core.runtime.test.TestResults.Failure;
import org.pkl.core.stdlib.base.PcfRenderer;
import org.pkl.core.util.EconomicMaps;
import org.pkl.core.util.MutableBoolean;
import org.pkl.core.util.MutableReference;

final class ExamplesRunner extends AbstractRunner {
  void run(VmTyped testModule, TestResults results, ModuleInfo info, boolean overwrite) {
    var examples = VmUtils.readMember(testModule, Identifier.EXAMPLES);
    if (examples instanceof VmNull) return;

    var moduleUri = info.getModuleKey().getUri();
    if (!moduleUri.getScheme().equalsIgnoreCase("file")) {
      throw new VmExceptionBuilder()
          .evalError("cannotEvaluateNonFileBasedTestModule", moduleUri)
          .build();
    }

    var examplesMapping = (VmMapping) examples;
    var moduleFile = Path.of(moduleUri);
    var expectedOutputFile = moduleFile.resolveSibling(moduleFile.getFileName() + "-expected.pcf");
    var actualOutputFile = moduleFile.resolveSibling(moduleFile.getFileName() + "-actual.pcf");

    try {
      Files.deleteIfExists(actualOutputFile);
    } catch (IOException e) {
      throw new VmExceptionBuilder()
          .evalError("ioErrorWritingTestOutputFile", actualOutputFile)
          .withCause(e)
          .build();
    }
    try {
      if (overwrite) {
        Files.deleteIfExists(expectedOutputFile);
      }
    } catch (IOException e) {
      throw new VmExceptionBuilder()
          .evalError("ioErrorWritingTestOutputFile", expectedOutputFile)
          .withCause(e)
          .build();
    }

    if (Files.exists(expectedOutputFile)) {
      doRunAndValidateExamples(examplesMapping, expectedOutputFile, actualOutputFile, results);
    } else {
      doRunAndWriteExamples(examplesMapping, expectedOutputFile, results);
    }
  }

  private void doRunAndValidateExamples(
      VmMapping examples, Path expectedOutputFile, Path actualOutputFile, TestResults results) {
    var expectedExampleOutputs = loadExampleOutputs(expectedOutputFile);
    var actualExampleOutputs = new MutableReference<VmDynamic>(null);
    var allGroupsSucceeded = new MutableBoolean(true);
    examples.forceAndIterateMemberValues(
        (groupKey, groupMember, groupValue) -> {
          var testName = String.valueOf(groupKey);
          var group = (VmListing) groupValue;
          var expectedGroup =
              (VmDynamic) VmUtils.readMemberOrNull(expectedExampleOutputs, groupKey);

          if (expectedGroup == null) {
            results.newResult(
                testName,
                Failure.buildExamplePropertyMismatchFailure(
                    getDisplayUri(groupMember), String.valueOf(groupKey), true));
            return true;
          }

          if (group.getLength() != expectedGroup.getLength()) {
            results.newResult(
                testName,
                Failure.buildExampleLengthMismatchFailure(
                    getDisplayUri(groupMember),
                    String.valueOf(groupKey),
                    expectedGroup.getLength(),
                    group.getLength()));
            return true;
          }

          var groupSucceeded = new MutableBoolean(true);
          group.forceAndIterateMemberValues(
              ((exampleIndex, exampleMember, exampleValue) -> {
                var expectedValue = VmUtils.readMember(expectedGroup, exampleIndex);

                var exampleValuePcf = renderAsPcf(exampleValue);
                var expectedValuePcf = renderAsPcf(expectedValue);

                if (!(exampleValuePcf.equals(expectedValuePcf))) {
                  if (actualExampleOutputs.isNull()) {
                    // immediately write and load `<fileName>-actual.pcf`
                    // so that we can generate deep link with correct line number for each
                    // mismatch
                    writeExampleOutputs(actualOutputFile, examples);
                    actualExampleOutputs.set(loadExampleOutputs(actualOutputFile));
                  }

                  groupSucceeded.set(false);

                  var expectedMember = VmUtils.findMember(expectedGroup, exampleIndex);
                  assert expectedMember != null;

                  var actualGroup =
                      (VmObjectLike) VmUtils.readMemberOrNull(actualExampleOutputs.get(), groupKey);
                  var actualMember =
                      actualGroup == null ? null : VmUtils.findMember(actualGroup, exampleIndex);
                  if (actualMember == null) {
                    // file was written earlier in this method;
                    // must have been tampered with by another process
                    throw new VmExceptionBuilder()
                        .evalError("invalidOutputFileStructure", actualOutputFile)
                        .build();
                  }

                  results.newResult(
                      testName,
                      Failure.buildExampleFailure(
                          getDisplayUri(exampleMember),
                          getDisplayUri(expectedMember),
                          expectedValuePcf,
                          getDisplayUri(actualMember),
                          exampleValuePcf));
                }

                return true;
              }));

          if (groupSucceeded.get()) {
            results.newResult(testName);
          } else {
            allGroupsSucceeded.set(false);
          }

          return true;
        });

    expectedExampleOutputs.iterateMembers(
        (groupKey, groupMember) -> {
          if (groupMember.isLocalOrExternalOrHidden()) {
            return true;
          }
          if (examples.getCachedValue(groupKey) == null) {
            allGroupsSucceeded.set(false);
            results.newResult(
                String.valueOf(groupKey),
                Failure.buildExamplePropertyMismatchFailure(
                    getDisplayUri(groupMember), String.valueOf(groupKey), false));
          }
          return true;
        });

    if (!allGroupsSucceeded.get() && actualExampleOutputs.isNull()) {
      writeExampleOutputs(actualOutputFile, examples);
    }
  }

  private void doRunAndWriteExamples(VmMapping examples, Path outputFile, TestResults results) {
    examples.forceAndIterateMemberValues(
        (groupKey, groupMember, groupValue) -> {
          results.newResult(String.valueOf(groupKey)).setExampleWritten(true);
          return true;
        });
    writeExampleOutputs(outputFile, examples);
  }

  private void writeExampleOutputs(Path outputFile, VmMapping examples) {
    var outputFileContent =
        new VmDynamic(
            VmUtils.createEmptyMaterializedFrame(),
            BaseModule.getDynamicClass().getPrototype(),
            EconomicMaps.of(
                Identifier.EXAMPLES,
                VmUtils.createSyntheticObjectProperty(Identifier.EXAMPLES, "examples", examples)),
            0);
    var builder = new StringBuilder();
    new PcfRenderer(builder, "  ", converter, false, false).renderDocument(outputFileContent);
    try {
      Files.writeString(outputFile, builder);
    } catch (IOException e) {
      throw new VmExceptionBuilder()
          .evalError("ioErrorWritingTestOutputFile", outputFile)
          .withCause(e)
          .build();
    }
  }

  private VmDynamic loadExampleOutputs(Path outputFile) {
    // load file manually to prevent it from being cached (won't need it again)
    String fileContent;
    try {
      fileContent = Files.readString(outputFile, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new VmExceptionBuilder()
          .evalError("ioErrorReadingTestOutputFile", outputFile)
          .withCause(e)
          .build();
    }
    var module =
        VmLanguage.get(null).loadModule(ModuleKeys.synthetic(outputFile.toUri(), fileContent));
    var examples = (VmDynamic) VmUtils.readMemberOrNull(module, Identifier.EXAMPLES);
    if (examples == null) {
      throw new VmExceptionBuilder().evalError("invalidOutputFileStructure", outputFile).build();
    }
    return examples;
  }
}
