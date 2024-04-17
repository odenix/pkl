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

import org.pkl.core.ast.member.ObjectMember;
import org.pkl.core.runtime.VmContext;
import org.pkl.core.runtime.VmMapping;
import org.pkl.core.runtime.VmObject;
import org.pkl.core.runtime.VmUtils;
import org.pkl.core.stdlib.PklConverter;
import org.pkl.core.stdlib.base.PcfRenderer;

/**
 * Base class for facts/properties/examples runners. A Runner instance has the same lifespan as the
 * Evaluator instance it belongs to.
 */
abstract class AbstractRunner {
  final PklConverter converter = new PklConverter(VmMapping.empty());

  final String renderAsPcf(Object pklValue) {
    var builder = new StringBuilder();
    new PcfRenderer(builder, "  ", converter, false, false).renderValue(pklValue);
    if (pklValue instanceof VmObject) {
      builder.insert(0, "new ");
    }
    return builder.toString();
  }

  final String getDisplayUri(ObjectMember member) {
    return VmUtils.getDisplayUri(
        member.getSourceSection(), VmContext.get(null).getFrameTransformer());
  }
}
