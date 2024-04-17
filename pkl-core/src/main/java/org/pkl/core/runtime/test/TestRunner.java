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

import org.pkl.core.BufferedLogger;
import org.pkl.core.StackFrameTransformer;
import org.pkl.core.runtime.ModuleInfo;
import org.pkl.core.runtime.TestModule;
import org.pkl.core.runtime.VmContext;
import org.pkl.core.runtime.VmException;
import org.pkl.core.runtime.VmExceptionBuilder;
import org.pkl.core.runtime.VmTyped;
import org.pkl.core.runtime.VmUtils;
import org.pkl.core.runtime.test.TestResults.Error;

/** Runs test results examples and facts. */
public class TestRunner {
  private final BufferedLogger logger;
  private final StackFrameTransformer stackFrameTransformer;
  private final FactsRunner factsRunner = new FactsRunner();
  private final PropertiesRunner propertiesRunner = new PropertiesRunner();
  private final ExamplesRunner examplesRunner = new ExamplesRunner();

  public TestRunner(BufferedLogger logger, StackFrameTransformer stackFrameTransformer) {
    this.logger = logger;
    this.stackFrameTransformer = stackFrameTransformer;
  }

  public TestResults run(VmTyped testModule, boolean overwrite) {
    var info = VmUtils.getModuleInfo(testModule);
    var results = new TestResults(info.getModuleName(), getDisplayUri(info));

    try {
      checkAmendsPklTest(testModule);
      factsRunner.run(testModule, results);
      propertiesRunner.run(testModule, results);
      examplesRunner.run(testModule, results, info, overwrite);
    } catch (VmException v) {
      var meta = results.newResult(info.getModuleName());
      meta.addError(new Error(v.getMessage(), v.toPklException(stackFrameTransformer)));
    }
    results.setErr(logger.getLogs());
    return results;
  }

  public void close() {
    propertiesRunner.close();
  }

  private void checkAmendsPklTest(VmTyped value) {
    var testModuleClass = TestModule.getModule().getVmClass();
    var moduleClass = value.getVmClass();
    while (moduleClass != testModuleClass) {
      moduleClass = moduleClass.getSuperclass();
      if (moduleClass == null) {
        throw new VmExceptionBuilder().typeMismatch(value, testModuleClass).build();
      }
    }
  }

  protected final String getDisplayUri(ModuleInfo moduleInfo) {
    return VmUtils.getDisplayUri(
        moduleInfo.getModuleKey().getUri(), VmContext.get(null).getFrameTransformer());
  }
}
